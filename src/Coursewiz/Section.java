package Coursewiz;

import Coursewiz.TimePeriod;
import Coursewiz.Meeting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Section extends Object implements Choice {
	/* **********************************************
	 * DATA
	 * **********************************************/
	
	public class Instructor {
		String lastName;
		String firstName;
		public Instructor(BasicDBObject instructor) {
			lastName = instructor.getString("last_name");
			firstName = instructor.getString("first_name");
		}
		public String toString() {
			return firstName + " " + lastName;
		}
	}
	
	// Section-specific data
	String id;
	String description;
	String notes;
	String location;
	SectionType type = SectionType.LECTURE;
	int spacesAvailable;
	int numberRegistered;
	boolean canceled;
	List<Instructor> instructors = new ArrayList<Instructor>();
	Course course;
	double units;
	
	List<Meeting> meetings = new ArrayList<Meeting>();
	
	public enum SectionType {LECTURE, DISCUSSION, LAB, QUIZ, LECTURELAB};
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	
	public Section() {
		
	}
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	public void addMeeting(Meeting m) {
		meetings.add(m);
	}

	public Boolean worksWith(Choice otherChoice) {
		Section otherSection = (Section)(otherChoice);
		
		// All of my meetings must work with all of your meetings
		for (Meeting myMeeting : meetings) {
			for (Meeting yourMeeting : otherSection.meetings) {
				if (!myMeeting.worksWith(yourMeeting))
					return false;
			}
		}
		
		return true;
	}
	
	public void loadFromDatabaseObject(BasicDBObject section) {
		// Set basic properties of the section
		id = section.getString("id");
		description = section.getString("description");
		notes = section.getString("notes");
		type = parseSectionType(section.getString("type"));
		spacesAvailable = Integer.parseInt(section.getString("spaces_available"));
		numberRegistered = Integer.parseInt(section.getString("number_registered"));
		canceled = section.getString("canceled").toUpperCase() == "Y";
		location = section.getString("location");
		if (location.equals("{ }")) {
			location = "TBA";
		}
		
		try {
			units = Double.parseDouble(section.getString("units"));
		} catch (NumberFormatException exception) {
			units = 0.0;
		}
		
		try {
			// There is more than one instructor
			BasicDBList instructorObject = (BasicDBList) section.get("instructor");
			for (Object thisObject : instructorObject) {
				BasicDBObject thisInstructor = (BasicDBObject) thisObject;
				Instructor newInstructor = new Instructor(thisInstructor);
				instructors.add(newInstructor);
			}
		} catch (java.lang.ClassCastException exception) {
			// Just one instructor
			instructors.add(new Instructor((BasicDBObject) section.get("instructor")));
		}
		
		
		try {
			// There is more than one meeting
			BasicDBList dayObject = (BasicDBList) section.get("day");
			if (dayObject != null) {
				String day0 = (String) dayObject.get(0);
				String day1 = (String) dayObject.get(1);
				
				BasicDBList startTimeObject = (BasicDBList) section.get("start_time");
				String st0 = (String) startTimeObject.get(0);
				String st1 = (String) startTimeObject.get(1);
				
				BasicDBList endTimeObject = (BasicDBList) section.get("end_time");
				String et0 = (String) endTimeObject.get(0);
				String et1 = (String) endTimeObject.get(1);
				
				Meeting meeting0 = new Meeting(day0, st0, et0);
				Meeting meeting1 = new Meeting(day1, st1, et1);
				
				meetings.add(meeting0);
				meetings.add(meeting1);
			}
		} catch (java.lang.ClassCastException exception) {
			Meeting firstMeeting = new Meeting(section.getString("day"),
					   section.getString("start_time"),
					   section.getString("end_time"));
			
			meetings.add(firstMeeting);
		}
	}
	
	/**
	 * Returns a map representing this Section. Useful for converting this section to JSON.
	 */
	public Map<String, Object> serialize() {
		Map<String, Object> sectionMap = new HashMap<String, Object>();
		sectionMap.put("status", getAvailable() ? "Open" : "Closed");
		sectionMap.put("location", location);
		
		List<Map<String,Object>> meetingArray = new ArrayList<Map<String,Object>>();
		for (Meeting thisMeeting : meetings) {
			Map<String,Object> thisMeetingMap = new HashMap<String,Object>();
			thisMeetingMap.put("days", thisMeeting.serializeDays());
			thisMeetingMap.put("startTime", thisMeeting.serializeStartTime());
			thisMeetingMap.put("endTime", thisMeeting.serializeEndTime());
			meetingArray.add(thisMeetingMap);
		}
		sectionMap.put("meetings", meetingArray);
		
		sectionMap.put("sectionNumber", id);
		sectionMap.put("type", serializeSectionType(type));
		sectionMap.put("courseTitle", course.title);
		sectionMap.put("id", course.getPrettyID());
		return sectionMap;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	public void setType(SectionType st) {
		type = st;
	}
	
	public void setCourse(Course newCourse) {
		course = newCourse;
	}
	
	/**
	 * Returns whether or not this section could be theoretically registered for.
	 * False if this section has been canceled or there aren't any spots.
	 */
	public boolean getAvailable() {
		return (!canceled && numberRegistered < spacesAvailable);
	}
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
	
	public String toString() {
		String returnString = "";
		returnString = "Section: " + id;
		for (Meeting meeting : meetings) {
			returnString += meeting.toString() + " ";
		}
		return returnString;
	}
	
	private static SectionType parseSectionType(String sectionType) {
		sectionType = sectionType.toLowerCase();
		
		if (sectionType.equals("lec"))
			return SectionType.LECTURE;
		else if (sectionType.equals("dis"))
			return SectionType.DISCUSSION;
		else if (sectionType.equals("lab"))
			return SectionType.LAB;
		else if (sectionType.equals("qz"))
			return SectionType.QUIZ;
		
		// should be lec-lab
		return SectionType.LECTURELAB;
	}
	
	private static String serializeSectionType(SectionType type) {
		if (type == SectionType.LECTURE)
			return "lecture";
		else if (type == SectionType.DISCUSSION)
			return "discussion";
		else if (type == SectionType.LAB)
			return "lab";
		else if (type == SectionType.QUIZ)
			return "quiz";
		
		return "lecture-lab";
	}
	
	/* **********************************************
	 * STATISTICS
	 * **********************************************/
	
	public double classTimeOnDay(byte day) {
		double total = 0.0;
		for (Meeting thisMeeting : meetings) {
			total += thisMeeting.timeOnDay(day);
		}
		return total;
	}
	
	public double earliestStartTime() {
		double startTime = 10000.0;
		for (Meeting thisMeeting : meetings) {
			double thisStartTime = thisMeeting.timePeriod.getDecimalStartTime();
			if (thisStartTime < startTime) {
				startTime = thisStartTime;
			}
		}
		return startTime;
	}
	
	public Set<Meeting> meetingsOnDay(byte day) {
		Set<Meeting> meetingSet = new HashSet<Meeting>();
		for (Meeting thisMeeting : meetings) {
			if (thisMeeting.meetsOnDay(day)) {
				meetingSet.add(thisMeeting);
			}
		}
		return meetingSet;
	}
}
