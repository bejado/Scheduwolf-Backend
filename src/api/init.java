package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class init {

	static MongoClient mongoClient = null;
	
	/*
	 * Downloads all the departments and stores the information in the Mongo database.
	 */
	public static void main(String[] args) throws IOException, UnknownHostException {
		
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
					mongoDepartments.add(newDepartment);
				} else if (internalDepartment.isJsonArray()) {
					// The internal department is an array of departments
					JsonArray internalDepartmentList = internalDepartment.getAsJsonArray();
					for (JsonElement thisInternalDepartment : internalDepartmentList) {
						JsonObject thisInternalDepartmentObject = thisInternalDepartment.getAsJsonObject();
						BasicDBObject newDepartment = new BasicDBObject();
						newDepartment.put("code", thisInternalDepartmentObject.get("code").getAsString());
						newDepartment.put("name", thisInternalDepartmentObject.get("name").getAsString());
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
			System.out.println("Downloading data for " + departmentCode + "...");
			schedule.refreshMongoForDepartment(departmentCode);
			System.out.println("Complete!\n");
		}
		
		// Close the mongoClient
		mongoClient.close();

	}

}
