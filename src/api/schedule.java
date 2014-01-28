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
		public String departmentAbbreviation;
		public int number;
		public String suffix;
		
		public CourseParser(String rawCourseString) {
			rawCourseString = removeWhitespace(rawCourseString);
			departmentAbbreviation = removeNonLetters(rawCourseString).toUpperCase();
			number = Integer.parseInt(removeNonNumbers(rawCourseString));
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
		
		// Create a course for each of the courses given and add it to the course group
		CourseGroup group = solver.new CourseGroup();
		for (String thisCourseString : coursesList) {
			// Create a new course and a course string parser
			Course newCourse = new Course();
			CourseParser parser = new CourseParser(thisCourseString);
			
			// Load the course information from our Mongo database
			BasicDBObject courseDB = loadCourseFromMongo(parser.departmentAbbreviation, parser.number);
			newCourse.loadFromDatabase(courseDB);
			
			// Calculate its potential classes
			newCourse.calculateClasses();
			
			// Remove unavailable sections
			newCourse.filterSections(excludeSectionsList);
			
			group.addCourse(newCourse);
		}
		
		// Solve!
		solver.addCourseGroup(group);
		solver.solve();
		Solution solution = solver.getSolution();
		
		// Set up our writer to output data to the client
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		
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
		// Load the correct department from MongoDB
		BasicDBObject department = null;
		department = findDepartmentInDatabase(departmentAbb);
		
		// If we couldn't find the department, we should try loading it
		if (department == null) {
			refreshMongoForDepartment(departmentAbb);
			department = findDepartmentInDatabase(departmentAbb);
			if (department == null) {
				return null;
			}
		}
		
		// Get the list of courses
		BasicDBObject offeredCourses = (BasicDBObject) department.get("OfferedCourses");
		BasicDBList courses = (BasicDBList) offeredCourses.get("course");
		
		// Find the correct course
		BasicDBObject foundCourse = null;
		for (Object courseObject : courses) {
			BasicDBObject thisCourse = (BasicDBObject) courseObject;
			BasicDBObject courseData = (BasicDBObject) thisCourse.get("CourseData");
			String number = courseData.getString("number");
			if (Integer.parseInt(number) == courseNumber) {
				foundCourse = courseData;
				break;
			}
		}
		
		return foundCourse;
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
