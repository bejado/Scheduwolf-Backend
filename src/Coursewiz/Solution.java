package Coursewiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {
	/* **********************************************
	 * DATA
	 * **********************************************/
	List<Schedule> schedules = new ArrayList<Schedule>();
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	public Solution() {
		
	}
	
	public Solution(List<Schedule> initSchedules) {
		schedules = initSchedules;
	}
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	public Map<String, Object> serialize() {
		Map<String, Object> solutionMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> solutionArray = new ArrayList<Map<String, Object>>();
		for (Schedule thisSchedule : schedules) {
			solutionArray.add(thisSchedule.serialize());
		}
		
		List<Map<String, Object>> sectionArray = new ArrayList<Map<String, Object>>();
		for (Section thisSection : getSections()) {
			sectionArray.add(thisSection.serialize());
		}
		
		solutionMap.put("solutions", solutionArray);
		solutionMap.put("sections", sectionArray);
		
		return solutionMap;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	/**
	 * Returns a list of schedule objects belonging to this solution.
	 */
	public List<Schedule> getSchedules() {
		return schedules;
	}
	
	/**
	 * Returns a set of the individual sections that comprise all the schedules in this solution.
	 */
	public Set<Section> getSections() {
		Set<Section> sectionSet = new HashSet<Section>();
		for (Schedule thisSchedule : schedules) {
			sectionSet.addAll(thisSchedule.getSections());
		}
		return sectionSet;
	}
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
	
}
