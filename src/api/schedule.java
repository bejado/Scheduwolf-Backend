package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Coursewiz.Course;
import Coursewiz.Schedule;
import Coursewiz.ScheduleSolver;
import Coursewiz.ScheduleSolver.CourseGroup;
import Coursewiz.Solution;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Servlet implementation class JSONTest
 */
//@WebServlet("/JSONTest")
public class schedule extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Grab a hold of the singleton
	static MongoClient mongoClient = MongoUtil.getMongo();
	
	static Gson gson = new Gson();
	
	public static class CourseParser {
		public String departmentAbbreviation = "";
		public int number = 0;
		public String suffix = "";
		
		public CourseParser(String rawCourseString) {
			rawCourseString = removeWhitespace(rawCourseString);
			departmentAbbreviation = removeNonLetters(rawCourseString).toUpperCase();
			number = Integer.parseInt(removeNonNumbers(rawCourseString));
		}
		
		public String getPrettyName() {
			return departmentAbbreviation + " " + Integer.toString(number) + suffix;
		}
		
		String removeWhitespace(String testString) {
			return testString.replaceAll("\\s", "");
		}
		
		String removeNonLetters(String testString) {
			return testString.replaceAll("[^a-zA-Z]", "");
		}
		
		String removeNonNumbers(String testString) {
			return testString.replaceAll("\\D", "");
		}
	}
       
    /**
     * @throws UnknownHostException 
     * @see HttpServlet#HttpServlet()
     */
    public schedule() {
        super();
        System.out.println("Constructor!");
    }
    
    public void init(ServletConfig config) {
    	System.out.println("Init!");
    }
    
    public void destroy() {
    	System.out.println("Destroy!");
    	MongoUtil.closeMongo();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		
		// Get the JSON courses parameter given to us
		String coursesJSON = request.getParameter("courses");
		
		// Get the JSON priorities parameter given to us
		String prioritiesJSON = request.getParameter("priorities");
		
		// Get the JSON exclude_sec parameter given to us
		String excludeSecJSON = request.getParameter("exclude_sec");
		
		// Get the reload parameter
		String reload = request.getParameter("reload");
		
		if (reload != null) {
			refreshMongoForDepartment(reload);
			return;
		}
		
		// Set up our writer to output data to the client
		PrintWriter out = response.getWriter();
		response.setContentType("text/json");
		
		// Parse the returned JSON data
		Type listType = new TypeToken<List<String>>() {}.getType();
		List<String> coursesList = gson.fromJson(coursesJSON, listType);
		List<String> prioritiesList = null;
		if (prioritiesJSON != null) {
			prioritiesList = gson.fromJson(prioritiesJSON, listType);
		}
		
		List<String> excludeSectionsList = null;
		if (excludeSecJSON != null) {
			excludeSectionsList = gson.fromJson(excludeSecJSON, listType);
		}
		
		if (coursesList == null || coursesList.size() == 0) {
			return;
		}
		
		// Create the schedule solver object to be used later
		ScheduleSolver solver = new ScheduleSolver();
		solver.setPriorities(prioritiesList);
		
		// A list to store the Courses that have no classes
		List<String> noClasses = new ArrayList<String>();
		
		// Create a course for each of the courses given and add it to the course group
		CourseGroup group = solver.new CourseGroup();
		for (String thisCourseString : coursesList) {
			// Create a new course and a course string parser
			Course newCourse = new Course();
			CourseParser parser = new CourseParser(thisCourseString);
			
			// Load the course information from our Mongo database
			BasicDBObject courseDB = loadCourseFromMongo(parser.departmentAbbreviation, parser.number);
			if (courseDB == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				out.println(parser.getPrettyName());
				return;
			}
			newCourse.loadFromDatabase(courseDB);
			
			// Calculate its potential classes
			newCourse.calculateClasses();
			
			// Remove unavailable sections
			newCourse.filterSections(excludeSectionsList);
			
			// If there aren't any classes, add it to the list but not the course group
			if (newCourse.getNumberOfClasses() == 0) {
				noClasses.add(newCourse.title);
			} else {
				group.addCourse(newCourse);
			}
		}
		
		// If there's a Course with no classes, error out
		if (noClasses.size() > 0) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			out.println(gson.toJson(noClasses));
			return;
		}
		
		// Solve!
		solver.addCourseGroup(group);
		boolean result = solver.solve();
		Solution solution = solver.getSolution();
		
		// If we failed to generate a solution
		if (!result) {
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			//out.print("{'Error': 'Not possible'}");
			return;
		}
		
		Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		out.print(gson.toJson(solution.serialize()));
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	public static BasicDBObject loadCourseFromMongo(String departmentAbb, int courseNumber) throws IOException {
		DB db = mongoClient.getDB("Scheduwolf_overhall");
		DBCollection collection = db.getCollection("Courses");
		
		// Form a query to find the correct course
		BasicDBObject andQuery = new BasicDBObject();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		obj.add(new BasicDBObject("prefix", departmentAbb));
		obj.add(new BasicDBObject("number", Integer.toString(courseNumber)));
		andQuery.put("$and", obj);
		
		DBCursor cursor = collection.find(andQuery);
		List<DBObject> results = cursor.toArray();
		BasicDBObject course = null;
		if (results.size() > 0) {
			course = (BasicDBObject) results.get(0);
		}
		return course;
	}
	
	public static void refreshMongoForDepartment(String department) throws IOException {
		// We want to load the department data from USC's SOC API
		String scheduleofClassesAPI = "http://web-app.usc.edu/ws/soc/api/classes/" + department + "/20141";
		
		// Open the connection to the URL
		URL url = new URL(scheduleofClassesAPI);
		URLConnection connection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		// Read the data
		String inputLine;
		String json = "";
		while ((inputLine = in.readLine()) != null) {
			json += inputLine;
		}
		in.close();
		
		// Parse the returned JSON data
		Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
		Map<String, Object> jsonMap = gson.fromJson(json, mapType);
		
		// Store the returned JSON in Mongo
		DB db = mongoClient.getDB("Coursewiz");
		DBCollection collection = db.getCollection("departments");
		BasicDBObject newObject = new BasicDBObject(jsonMap);
		collection.insert(newObject);
	}
	
	private static BasicDBObject findDepartmentInDatabase(String departmentAbb) {
		DB db = mongoClient.getDB("Coursewiz");
		DBCollection collection = db.getCollection("departments");
		BasicDBObject query = new BasicDBObject("Dept_Info.abbreviation", departmentAbb);
		DBCursor cursor = collection.find(query);
		List<DBObject> results = cursor.toArray();
		BasicDBObject department = null;
		if (results.size() > 0) {
			department = (BasicDBObject) results.get(0);
		}
		return department;
	}
}
