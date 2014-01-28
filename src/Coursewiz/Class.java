package Coursewiz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of a Course with specific sections.
 * @author bdoherty
 *
 */
public class Class extends Object implements Choice {
	/* **********************************************
	 * DATA
	 * **********************************************/
	String name;
	List<Section> sections = new ArrayList<Section>();
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	
	public Class() {
		this("");
	}
	
	public Class(String initName) {
		this(initName, null);
	}
	
	/**
	 * Initialize this Class with sections.
	 */
	public Class(String initName, List<Section> initialSections) {
		name = initName;
		if (initialSections != null) {
			sections = initialSections; 
		}
	}
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	/**
	 * Add a section to this class's sections.
	 */
	public void addSection(Section section) {
		sections.add(section);
	}
	
	public Boolean worksWith(Choice otherChoice) {
		Class otherClass = (Class)otherChoice;
		
		// All of my sections must work with all of your sections
		for (Section mySection : sections) {
			for (Section yourSection : otherClass.sections) {
				if (!mySection.worksWith(yourSection))
					return false;
			}
		}
		
		return true;
	}
	
	public Boolean containsASection(List<String> sectionCheckList) {
		for (Section thisSection : sections) {
			if (sectionCheckList.contains(thisSection.id))
				return true;
		}
		return false;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
	
	public String toString() {
		String returnString = name + "\n";
		for (Section thisSection : sections) {
			returnString += thisSection + "\n";
		}
		return returnString;
	}
	
	/* **********************************************
	 * STATISTICS
	 * **********************************************/
	
	public double classTimeOnDay(byte day) {
		double total = 0.0;
		for (Section thisSection : sections) {
			total += thisSection.classTimeOnDay(day);
		}
		return total;
	}

	public double earliestStartTime() {
		double startTime = 10000.0;
		for (Section thisSection : sections) {
			double thisStartTime = thisSection.earliestStartTime();
			if (thisStartTime < startTime) {
				startTime = thisStartTime;
			}
		}
		return startTime;
	}
	
	public Set<Meeting> meetingsOnDay(byte day) {
		Set<Meeting> meetingSet = new HashSet<Meeting>();
		for (Section thisSection : sections) {
			meetingSet.addAll(thisSection.meetingsOnDay(day));
		}
		return meetingSet;
	}
}
