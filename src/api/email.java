package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * Servlet implementation class email
 */
@WebServlet("/email")
public class email extends HttpServlet {
	// Grab a hold of the singleton
	MongoClient mongoClient = MongoUtil.getMongo();
	
	private static final long serialVersionUID = 1L;
	
    public void init(ServletConfig config) {
    	System.out.println("Init!");
    }
    
    public void destroy() {
    	System.out.println("Destroy!");
    	MongoUtil.closeMongo();
    }
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public email() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get the email parameter
		String userEmail = request.getParameter("email");
		
		// Store the email in Mongo
		if (!userEmail.equals("")) {
			DB db = mongoClient.getDB("Scheduwolf");
			DBCollection collection = db.getCollection("emails");
			BasicDBObject newObject = new BasicDBObject();
			newObject.put("email", userEmail);
			collection.insert(newObject);
		}
		
		// Set up our writer to output data to the client
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.print("Success!");
	}

}
