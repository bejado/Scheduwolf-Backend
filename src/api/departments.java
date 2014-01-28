package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/**
 * Servlet implementation class departments
 */
@WebServlet("/departments")
public class departments extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	// Grab a hold of the singleton
	MongoClient mongoClient = MongoUtil.getMongo();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public departments() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set up our writer to output data to the client
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		
		// Get the departments from Mongo
		DB db = mongoClient.getDB("Scheduwolf");
		DBCollection collection = db.getCollection("departments");
		DBCursor cursor = collection.find();
		List<DBObject> results = cursor.toArray();
		
		// Create the output list
		List<BasicDBObject> outputList = new ArrayList<BasicDBObject>();
		for (DBObject thisResult : results) {
			Map<String, String> thisMap = thisResult.toMap();
			BasicDBObject newObject = new BasicDBObject();
			newObject.put("code", thisMap.get("code"));
			newObject.put("name", thisMap.get("name"));
			outputList.add(newObject);
		}
		
		// Output the JSON
		out.print(JSON.serialize(outputList));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
