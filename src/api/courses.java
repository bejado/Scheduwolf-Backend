package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Servlet implementation class courses
 */
@WebServlet("/courses")
public class courses extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Grab a hold of the singleton
	MongoClient mongoClient = MongoUtil.getMongo();
	
    public void init(ServletConfig config) {
    }
    
    public void destroy() {
    	MongoUtil.closeMongo();
    }
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public courses() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		// Get department
		String departmentSearch = request.getParameter("department").toUpperCase();
		
		// Set up our writer to output data to the client
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		
		// Get the department
		DB db = mongoClient.getDB("Coursewiz");
		DBCollection collection = db.getCollection("departments");
		BasicDBObject query = new BasicDBObject("Dept_Info.abbreviation", departmentSearch);
		DBCursor cursor = collection.find(query);
		List<DBObject> results = cursor.toArray();
		BasicDBObject department = null;
		if (results.size() > 0) {
			department = (BasicDBObject) results.get(0);
		}
		
		if (department == null) {
			return;
		}
		
		List<Map<String,Object>> courses = new ArrayList<Map<String,Object>>();
		
		BasicDBObject offeredCourses = (BasicDBObject) department.get("OfferedCourses");
		BasicDBList coursesObject = (BasicDBList) offeredCourses.get("course");
		for (Object thisCourse : coursesObject) {
			BasicDBObject thisCourseObject = (BasicDBObject) thisCourse;
			BasicDBObject courseData = (BasicDBObject) thisCourseObject.get("CourseData");
			
			Map<String, Object> course = new HashMap<String, Object>();
			course.put("number", courseData.getString("number"));
			course.put("name", courseData.getString("title"));
			try {
				course.put("units", Double.parseDouble(courseData.getString("units")));
			} catch (java.lang.NumberFormatException exception) {
				course.put("units", 0.0);
			}
			courses.add(course);
		}
		
		Gson gsonBuilder = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		out.print(gsonBuilder.toJson(courses));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
