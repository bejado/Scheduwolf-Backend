package Coursewiz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleComparator implements Comparator<Schedule> {
	
	List<String> priorities = null;
	
	final static double EPSILON =	0.00001;
	
	/**
	 * Set the priorities for this comparator to use for comparing.
	 */
	public void setPriorities(List<String> p) {
		priorities = p;
	}

	public int compare(Schedule o1, Schedule o2) {
		int comparison = 0;
		
		// If we haven't loaded any priorities, just return 0
		if (priorities == null)
			return comparison;
		
		// Go through each priority, and stop when you have a comparison that isn't equal
		for (String priority : priorities) {
			comparison = compareForPriorityString(o1, o2, priority);
			if (comparison == 0) {
				continue;
			}
			break;
		}
		
		// Return that comparison
		return comparison;
	}
	
	private int compareForPriorityString(Schedule o1, Schedule o2, String priority) {
		int comparison;
		if (priority.equals("No class on Friday")) {
			comparison = compareClassTimeOnDay(o1, o2, Meeting.FRIDAY);
		} else if (priority.equals("Later start times")) {
			comparison = compareEarliestStartTime(o1, o2);
			if (comparison == 0) {
				comparison = compareAverageStartTime(o1, o2);
			}
		} else if (priority.equals("Days of class per week")) {
			comparison = compareClassDaysPerWeek(o1, o2);
		} else { // class time on day
			comparison = compareClassTimeOnDay(o1, o2, Meeting.ALL_DAYS);
		}
		
		return comparison;
	}
	
	private int compareClassTimeOnDay(Schedule o1, Schedule o2, byte day) {
		double o1time = o1.classTimeOnDay(day);
		double o2time = o2.classTimeOnDay(day);
		
		if (Math.abs(o2time - o1time) < EPSILON) return 0;
		if (o1time < o2time) return -1;
		return 1;
	}
	
	private int compareEarliestStartTime(Schedule o1, Schedule o2) {
		double o1time = o1.earliestStartTime();
		double o2time = o2.earliestStartTime();
		
		if (Math.abs(o2time - o1time) < EPSILON) return 0;
		if (o1time > o2time) return -1;
		return 1;
	}
	
	private int compareAverageStartTime(Schedule o1, Schedule o2) {
		double o1time = o1.averageStartTime();
		double o2time = o2.averageStartTime();
		
		if (Math.abs(o2time - o1time) < EPSILON) return 0;
		if (o1time > o2time) return -1;
		return 1;
	}
	
	/**
	 * Compares by the number of days per week the schedules have courses.
	 * For example, Ben Doherty only had class 4 days a week Fall 2012.
	 * It was amazing.
	 * @return int
	 */
	private int compareClassDaysPerWeek(Schedule o1, Schedule o2) {
		int o1Days = o1.classDaysPerWeek();
		int o2Days = o2.classDaysPerWeek();
		
		if (o1Days == o2Days) return 0;
		if (o1Days < o2Days) return -1;
		return 1;
	}

}
