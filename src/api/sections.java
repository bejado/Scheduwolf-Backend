package api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Servlet implementation class sections
 */
@WebServlet("/sections")
public class sections extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public sections() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get the required parameters
		String department = request.getParameter("department");
		String classNumberRaw = request.getParameter("number");
		int classNumber;
		try {
			classNumber = Integer.parseInt(classNumberRaw);
		} catch (java.lang.NumberFormatException exception) {
			return;
		}
		if (department.equals("") || classNumber <= 0) {
			return;
		}
		
		// Load the course
		BasicDBObject course = schedule.loadCourseFromMongo(department, classNumber);
		if (course == null) {
			return;
		}
		
		// Create an array out output to
		BasicDBList outputSectionList = new BasicDBList();
		
		// Get the sections of the course
		//DBObject courseData = (DBObject) course.get("CourseData");
		DBObject sectionData = (DBObject) course.get("SectionData");
		
		try {
			BasicDBList sectionArray = (BasicDBList) sectionData;
			
			for (Object thisSectionObject : sectionArray) {
				BasicDBObject thisSection = (BasicDBObject) thisSectionObject;
				
				BasicDBObject sectionMap = new BasicDBObject();
				sectionMap.put("id", thisSection.get("id"));
				sectionMap.put("start_time", thisSection.get("start_time"));
				sectionMap.put("end_time", thisSection.get("end_time"));
				sectionMap.put("location", thisSection.get("location"));
				sectionMap.put("spaces_available", thisSection.get("spaces_available"));
				sectionMap.put("canceled", thisSection.get("canceled"));
				sectionMap.put("number_registered", thisSection.get("number_registered"));
				sectionMap.put("type", thisSection.get("type"));
				sectionMap.put("day", thisSection.get("day"));
				
				outputSectionList.add(sectionMap);
			}
		} catch (java.lang.ClassCastException exception) {
			BasicDBObject sectionMap = new BasicDBObject();
			BasicDBObject thisSection = (BasicDBObject) sectionData;
			sectionMap.put("id", thisSection.get("id"));
			sectionMap.put("start_time", thisSection.get("start_time"));
			sectionMap.put("end_time", thisSection.get("end_time"));
			sectionMap.put("location", thisSection.get("location"));
			sectionMap.put("spaces_available", thisSection.get("spaces_available"));
			sectionMap.put("canceled", thisSection.get("canceled"));
			sectionMap.put("number_registered", thisSection.get("number_registered"));
			sectionMap.put("type", thisSection.get("type"));
			sectionMap.put("day", thisSection.get("day"));
			
			outputSectionList.add(sectionMap);
		}
		
		// Set up our writer to output data to the client
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		
		out.print(outputSectionList.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
