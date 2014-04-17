package api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * Servlet implementation class log
 */
@WebServlet("/log")
public class log extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	// Mongo
	MongoClient mongo = MongoUtil.getMongo();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public log() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get parameter
		String department = request.getParameter("department");
		String rawNumber = request.getParameter("number");
		
		// Parse the raw number
		int classNumber;
		try {
			classNumber = Integer.parseInt(rawNumber);
		} catch (java.lang.NumberFormatException exception) {
			return;
		}
		
		// Save the results to Mongo
		DB db = mongo.getDB("Scheduwolf_overhall");
		DBCollection collection = db.getCollection("CourseSearchCount");
		
		BasicDBObject query = new BasicDBObject();
		query.put("_id", department + Integer.toString(classNumber));
		
		BasicDBObject obj = new BasicDBObject();
		obj.put("$inc", new BasicDBObject("count", 1));
		
		collection.update(query, obj, true, false);
	}

}
