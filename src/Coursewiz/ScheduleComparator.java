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
		
		if (priorities == null)
			return comparison;
		
		for (String priority : priorities) {
			comparison = compareForPriorityString(o1, o2, priority);
			if (comparison == 0) {
				continue;
			}
			break;
		}
		
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
		} else {
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

}
