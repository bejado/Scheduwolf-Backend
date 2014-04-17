package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.node.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class init {

	// Mongo
	static MongoClient mongoClient = null;
	
	// Open the connection with elastic search
	static Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
	
	// Convenience GSON
	static Gson gson = new Gson();
	
	/*
	 * Downloads all the departments and stores the information in the Mongo database.
	 */
	public static void main(String[] args) throws IOException, UnknownHostException {
		// Limit departments for Debug purposes
		List<String> limit = new ArrayList<String>();
		//limit.add("EE");
		
		// Set up Mongo and Gson
		mongoClient = new MongoClient("localhost");
		
		// We want to load the department data from USC's SOC API
		String scheduleofClassesAPI = "http://web-app.usc.edu/ws/soc/api/depts/20141";
		
		// We're going to store the departments in a list
		List<DBObject> mongoDepartments = new ArrayList<DBObject>();
		
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
		JsonParser parser = new JsonParser();
		JsonObject departmentMap = parser.parse(json).getAsJsonObject();
		
		// Get the departments array
		JsonArray departmentsList = departmentMap.get("department").getAsJsonArray();
		if (departmentsList == null) {
			System.out.println("The API returned no departments.");
			mongoClient.close();
			return;
		}
		
		// Iterate through the departments
		for (JsonElement thisDepartment : departmentsList) {
			
			JsonObject thisDepartmentObject = thisDepartment.getAsJsonObject();
			JsonElement internalDepartment = thisDepartmentObject.get("department");
			
			// Check to see if the department has an internal "department" object
			// If so, we only want to add it to the departments list
			if (internalDepartment != null) {
				if (internalDepartment.isJsonObject()) {
					// The internal department is just one object
					JsonObject internalDepartmentMap = internalDepartment.getAsJsonObject();
					BasicDBObject newDepartment = new BasicDBObject();
					newDepartment.put("code", internalDepartmentMap.get("code").getAsString());
					newDepartment.put("name", internalDepartmentMap.get("name").getAsString());
					
					// Add it to our Mongo payload
					mongoDepartments.add(newDepartment);
					
				} else if (internalDepartment.isJsonArray()) {
					// The internal department is an array of departments
					JsonArray internalDepartmentList = internalDepartment.getAsJsonArray();
					for (JsonElement thisInternalDepartment : internalDepartmentList) {
						JsonObject thisInternalDepartmentObject = thisInternalDepartment.getAsJsonObject();
						BasicDBObject newDepartment = new BasicDBObject();
						newDepartment.put("code", thisInternalDepartmentObject.get("code").getAsString());
						newDepartment.put("name", thisInternalDepartmentObject.get("name").getAsString());
						
						// Add it to our Mongo payload
						mongoDepartments.add(newDepartment);
					}
				}
			} else {
				// No internal department array, so just add myself
				BasicDBObject newDepartment = new BasicDBObject();
				newDepartment.put("code", thisDepartmentObject.get("code").getAsString());
				newDepartment.put("name", thisDepartmentObject.get("name").getAsString());
				mongoDepartments.add(newDepartment);
			}
			
		}
		
		// Store the returned JSON in Mongo
		DB db = mongoClient.getDB("Scheduwolf");
		DBCollection collection = db.getCollection("departments");
		collection.drop();
		collection.insert(mongoDepartments);
		
		// Delete previously downloaded data
		db = mongoClient.getDB("Coursewiz");
		DBCollection coursewizDepartments = db.getCollection("departments");
		coursewizDepartments.drop();
		
		// Download each department's data
		for (DBObject department : mongoDepartments) {
			String departmentCode = (String) department.get("code");
			
			if (limit.size() == 0 || limit.contains(departmentCode)) {
				System.out.println("Downloading data for " + departmentCode + "...");
				refreshMongoForDepartment(departmentCode);
				System.out.println("Complete!\n");
			}
		}
		
		// Close the mongoClient
		mongoClient.close();
		
		// Close the connection to elastic search
		//node.close();
	}
	
	/**
	 * Saves the courses from a department object in elastic search.
	 */
	private static void storeCourseInElastic(DBObject department) {
		DBObject offeredCourses = (DBObject) department.get("OfferedCourses");
		BasicDBList courses = new BasicDBList();
		try {
			courses = (BasicDBList) offeredCourses.get("course");
		} catch (java.lang.ClassCastException e) {
			BasicDBObject course = (BasicDBObject) offeredCourses.get("course");
			courses.add(course);
		}
		for (Object courseObject : courses) {
			DBObject course = (DBObject) courseObject;
			
			// Read the course from the object
			DBObject courseData = (DBObject) course.get("CourseData");
			String numberString = (String) courseData.get("number");
			String departmentString = (String) courseData.get("prefix");
			String titleString = (String) courseData.get("title");
			
			// Store the course in elastic
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("number", numberString);
			json.put("department", departmentString);
			json.put("title", titleString);
			json.put("common", departmentString + " " + numberString);
			IndexResponse respose = client.prepareIndex()
					.setSource(json)
					.setIndex("courses")
					.setType("course")
					.setId(departmentString + numberString)
					.execute()
					.actionGet();
					
		}
	}
	
	/**
	 * Saves department data from USC's API to Mongo
	 */
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
		DBObject jsonMap = (DBObject) JSON.parse(json);
		
		// Store the returned JSON in Mongo
		DB db = mongoClient.getDB("Coursewiz");
		DBCollection collection = db.getCollection("departments");
		BasicDBObject newObject = new BasicDBObject();
		collection.insert(jsonMap);
		
		// Save the courses in elastic
		storeCourseInElastic(jsonMap);
	}

}
