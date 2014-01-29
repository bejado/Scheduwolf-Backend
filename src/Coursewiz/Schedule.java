package Coursewiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A combination of classes that make up a student's semester.
 * @author bdoherty
 *
 */
public class Schedule {
	/* **********************************************
	 * DATA
	 * **********************************************/
	List<Class> classes = new ArrayList<Class>();
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	public Schedule() {
		
	}
	
	/**
	 * Initialize this Schedule with classes.
	 */
	public Schedule(List<Class> initialClasses) {
		classes = initialClasses;
	}
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	/**
	 * Add a class to this schedule.
	 */
	public void addClass(Class newClass) {
		classes.add(newClass);
	}
	
	/**
	 * Returns a map representing this Schedule. Useful for converting to JSON.
	 */
	public Map<String, Object> serialize() {
		Map<String, Object> scheduleMap = new HashMap<String, Object>();
		scheduleMap.put("stats", serializeStatistics());
		scheduleMap.put("sections", serializeSections());
		return scheduleMap;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	/**
	 * Returns a set of the individual Sections that comprise this schedule. 
	 */
	public Set<Section> getSections() {
		Set<Section> sectionSet = new HashSet<Section>();
		for (Class thisClass : classes) {
			sectionSet.addAll(thisClass.sections);
		}
		return sectionSet;
	}
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	private Map<String, Object> serializeStatistics() {
		Map<String, Object> statisticsMap = new HashMap<String, Object>();
		statisticsMap.put("Average start time", averageStartTime());
		statisticsMap.put("Class time on Friday", classTimeOnDay(Meeting.FRIDAY));
		//statisticsMap.put("Average time between classes", 3.14);
		statisticsMap.put("Total class time", classTimeOnDay(Meeting.ALL_DAYS));
		//statisticsMap.put("Average distance between classes", 420);
		return statisticsMap;
	}
	
	private List<String> serializeSections() {
		List<String> sectionList = new ArrayList<String>();
		Set<Section> sections = getSections();
		for (Section thisSection : sections) {
			sectionList.add(thisSection.id);
		}
		return sectionList;
	}
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
	
	public String toString() {
		String returnString = "";
		for (Class thisClass : classes) {
			returnString += thisClass + "\n";
		}
		return returnString;
	}
	
	/* **********************************************
	 * STATISTICS
	 * **********************************************/
	
	public double classTimeOnDay(byte day) {
		double total = 0.0;
		for (Class thisClass : classes) {
			total += thisClass.classTimeOnDay(day);
		}
		return total;
	}
	
	public double earliestStartTime() {
		double startTime = 10000.0;
		for (Class thisClass : classes) {
			double thisStartTime = thisClass.earliestStartTime();
			if (thisStartTime < startTime) {
				startTime = thisStartTime;
			}
		}
		return startTime;
	}
	
	public Set<Meeting> meetingsOnDay(byte day) {
		Set<Meeting> meetingSet = new HashSet<Meeting>();
		for (Class thisClass : classes) {
			meetingSet.addAll(thisClass.meetingsOnDay(day));
		}
		return meetingSet;
	}
	
	/**
	 * Returns true if there is at least one meeting on day.
	 */
	public boolean atLeastOneMeetingOnDay(byte day) {
		// Go through each class in this schedule.
		// If there are any meetings on day, then we know that there is at least one.
		for (Class thisClass : classes) {
			if (thisClass.meetingsOnDay(day).size() > 0) {
				return true;
			}
		}
		return false;
	}
	
	public double averageStartTime() {
		double runningTotal = 0.0;
		int dayCount = 0;
		byte day = Meeting.SATURDAY;
		for (int dayNum = 0; dayNum < 7; dayNum++) {
			day = (byte) (Meeting.SATURDAY << dayNum);
			double startTime = startTimeForDay(day);
			if (startTime > 0) {
				runningTotal += startTime;
				dayCount++;
			}
		}
		double average = 0;
		if (dayCount > 0) {
			average = runningTotal / dayCount;
		}
		return average;
	}
	
	public double startTimeForDay(byte day) {
		double startTime = 0;
		boolean initial = true;
		Set<Meeting> meetings = meetingsOnDay(day);
		for (Meeting thisMeeting : meetings) {
			double thisStartTime = thisMeeting.timePeriod.getDecimalStartTime();
			if (thisStartTime < startTime || initial) {
				startTime = thisStartTime;
				initial = false;
			}
		}
		return startTime;
	}
	
	/**
	 * Returns the number of days per week this Schedule contains classes.
	 * @return int
	 */
	public int classDaysPerWeek() {
		byte dateBit = 0x40;
		int totalClassDays = 0;
		
		for (int day = 0; day < 7; day++) {
			// Is there class on this day?
			if (this.atLeastOneMeetingOnDay(dateBit)) {
				totalClassDays++;
			}
			
			// Go to the next date 
			dateBit = (byte) (dateBit >> 1);
		}
		
		return totalClassDays;
	}
}
