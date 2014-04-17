package Coursewiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Takes input courses and calculates potential schedules.
 * @author bdoherty
 *
 */
public class ScheduleSolver {
	/* **********************************************
	 * DATA
	 * **********************************************/
	public class CourseGroup {
		List<Course> courses = new ArrayList<Course>();
		int amount;
		
		public CourseGroup() {
			this(0);
		}
		
		public CourseGroup(int initAmount) {
			amount = initAmount;
		}
		
		public void addCourse(Course newCourse) {
			courses.add(newCourse);
		}
		
		public void setAmount(int newAmount) {
			amount = newAmount;
		}
	
		/**
		 * Removes the newest course from this group.
		 * @return true if there are courses left after removal,
		 *		   false if none
		 */
		public boolean removeNewestCourse() {
			if (courses.size() > 0) {
				courses.remove(courses.size() - 1);
				return courses.size() > 0;
			}
			return false;
		}
	}
	
	List<CourseGroup> courseGroups = new ArrayList<CourseGroup>();
	Solution solution = null;
	List<String> priorities = null;
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	/**
	 * Add a list of courses to this solver.
	 */
	public void addCourseGroup(CourseGroup newGroup) {
		courseGroups.add(newGroup);
	}
	
	/**
	 * Generates potential schedules that all work and populates the schedules of this solver.
	 * Retrieve the schedules via getSchedules()
	 */
	public boolean solve() {
		
		List<Schedule> schedules = new ArrayList<Schedule>();

		// This is the big solve!
		if (courseGroups.size() == 0) {
			return false;
		}
		
		// For now, we'll just use the first course group
		// TODO obviously, we need to update this
		CourseGroup theGroup = courseGroups.get(0);
		
		// If there aren't any courses, error out
		if (theGroup.courses.size() == 0) {
			return false;
		}
		
		// If we can't generate at least one solution, switch to an error state
		List<List<Class>> generatedSchedules;
		
		// Create a choice node for every course in the group and add the appropriate classes to it
		List<ChoiceNode> nodes = new ArrayList<ChoiceNode>();
		ChoiceNode previousNode = null;
		for (Course thisCourse : theGroup.courses) {
			// Create the new node
			ChoiceNode newNode = new ChoiceNode();
			newNode.setAmount(1);	// choose one class
			newNode.addChoices(thisCourse.classes);
			
			// Add it to our list
			nodes.add(newNode);
			
			// Link the previous node to this node
			if (previousNode != null) {
				previousNode.next = newNode;
			}
			previousNode = newNode;
		}
		
		// Calculate the schedules
		ChoiceNode firstNode = nodes.get(0);
		generatedSchedules = firstNode.generateChoices();
		
		if (generatedSchedules.size() == 0) {
			return false;
		}
		
		// Add the schedules to our list
		for (List<Class> schedule : generatedSchedules) {
			Schedule newSchedule = new Schedule(schedule);
			schedules.add(newSchedule);
		}
		
		// Rank the schedules
		ScheduleComparator comparator = new ScheduleComparator();
		comparator.setPriorities(priorities);
		Collections.sort(schedules, comparator);
		
		solution = new Solution(schedules);
		
		return true;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	/**
	 * Gets the solution to this schedule solve. Null until solve() has been called.
	 */
	public Solution getSolution() {
		return solution;
	}
	
	/**
	 * Set the priorities to sort the generated schedules by.
	 */
	public void setPriorities(List<String> p) {
		priorities = p;
	}
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
}
