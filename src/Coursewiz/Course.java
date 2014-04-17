package Coursewiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import Coursewiz.Section.SectionType;

/**
 * A Course offered by a university. Add sections to it, then calculate its potential classes.
 * @author bdoherty
 *
 */
public class Course extends Object implements Choice {
	/* **********************************************
	 * DATA
	 * **********************************************/
	
	// Course-specific data
	public String title;
	String departmentAbbreviation;
	int courseNumber;
	String sequence;
	String suffix;
	String description;
	double units;
	
	List<Section> sections = new ArrayList<Section>();
	List<Class> classes = new ArrayList<Class>();
	
	public enum SectionLinkType {ONE_OF_EACH};
	SectionLinkType linkType = SectionLinkType.ONE_OF_EACH;
	
	/* **********************************************
	 * CONSTRUCTORS
	 * **********************************************/
	
	public Course() {
		this("");
	}
	
	public Course(String n) {
		title = n;
	}
	
	/* **********************************************
	 * PUBLIC INTERFACE
	 * **********************************************/
	
	public String getPrettyID() {
		return departmentAbbreviation + " " + Integer.toString(courseNumber) + sequence;
	}
	
	/**
	 * Add a Section to this Course's list of sections. The order in which they are added does matter, depending on this
	 * course's sectionLink type.
	 * @param section
	 */
	public void addSection(Section section) {
		sections.add(section);
	}

	public Boolean worksWith(Choice otherChoice) {
		return true;
	}
	
	public String loadFromDatabase(BasicDBObject courseDB) {
		// Set basic properties of the course
		title = courseDB.getString("title");
		departmentAbbreviation = courseDB.getString("prefix");
		courseNumber = Integer.parseInt(courseDB.getString("number"));
		sequence = courseDB.getString("sequence");
		if (sequence.contains("{")) {
			sequence = "";
		}
		suffix = courseDB.getString("suffix");
		description = courseDB.getString("description");

		// Create sections and add them to this course
		try {
			BasicDBList sectionsList = (BasicDBList) courseDB.get("SectionData");
			for (Object sectionObject : sectionsList) {
				BasicDBObject thisSection = (BasicDBObject) sectionObject;
				addSectionFromDatabaseObject(thisSection);
			}
		} catch (java.lang.ClassCastException exception) {
			// There must only be one section, as we can't cast the object to a list
			BasicDBObject sectionObject = (BasicDBObject) courseDB.get("SectionData");
			addSectionFromDatabaseObject(sectionObject);
		}
		
		
		return stringSections();
	}
	
	/**
	 * Removes classes that contain unwanted sections.
	 * Must be called after calculateClasses()
	 * @param sections: A list of section numbers to exclude
	 */
	public void filterSections(List<String> excludeSections) {
		
		if (excludeSections == null || excludeSections.size() == 0)
			return;
		
		List<Class> newClasses = new ArrayList<Class>();
		for (Class thisClass : classes) {
			// If it's not in the list go ahead and add it
			if (!thisClass.containsASection(excludeSections)) {
					newClasses.add(thisClass);
			}
		}
		classes = newClasses;
	}
	
	/* **********************************************
	 * SETTERS / GETTERS
	 * **********************************************/
	
	/*
	 * Returns the total number of generated Classes.
	 */
	public int getNumberOfClasses() {
		return classes.size();
	}
	
	/**
	 * Returns the generated classes. Empty until calculateClasses() has been called.
	 */
	public List<Class> getClasses() {
		return classes;
	}
	
	public void setLinkType(SectionLinkType lt) {
		linkType = lt;
	}
	
	/**
	 * Calculates potential classes a student could take based on the link type. Clears any previous calculation.
	 * Each class is a list of sections that constitute a unique implementation of this course.
	 */
	public void calculateClasses() {
		classes.clear();
		
		switch (linkType) {
		case ONE_OF_EACH: default:
			
			calculateClassesOneOfEach();
			
		}
	}
	
	/* **********************************************
	 * PRIVATE METHODS
	 * **********************************************/
	
	private void calculateClassesOneOfEach() {
		// Create a map of section types to section nodes
		Map<Section.SectionType, ChoiceNode<Section>> nodes = new HashMap<Section.SectionType, ChoiceNode<Section>>();
		
		// Create the nodes, one for each section type
		for (int i = 0; i < sections.size(); i++) {
			Section thisSection = sections.get(i);
			
			ChoiceNode<Section> node = nodes.get(thisSection.type);
			if (node == null) {
				node = new ChoiceNode<Section>();
				node.setAmount(1);
				nodes.put(thisSection.type, node);
			}
			node.addChoice(thisSection);
		}
		
		// Connect the nodes
		ChoiceNode<Section> firstNode = null;
		ChoiceNode<Section> previousNode = null;
		for (ChoiceNode<Section> node : nodes.values()) {
			if (firstNode == null) {
				firstNode = node;
			} else {
				previousNode.next = node;
			}
			previousNode = node;
		}
		
		// Perform the calculation
		List<List<Section>> results = null;
		if (firstNode != null) {
			results = firstNode.generateChoices();
		}
		
		// Turn the results into classes and add them to our list of classes
		if (results != null) {
			for (List<Section> thisClass : results) {
				Class newClass = new Class(title, thisClass);
				classes.add(newClass);
			}
		}
	}
	
	private void addSectionFromDatabaseObject(BasicDBObject section) {
		Section newSection = new Section();
		newSection.loadFromDatabaseObject(section);
		newSection.setCourse(this);
		sections.add(newSection);
	}
	
	/* **********************************************
	 * UTILITY
	 * **********************************************/
	
	public String toString() {
		return title;
	}
	
	public String stringClasses() {
		String returnString = "Classes for " + title + "\n";
		for (Class thisClass : classes) {
			returnString += thisClass + "\n";
		}
		return returnString;
	}
	
	public String stringSections() {
		String returnString = "Sections for " + title + "\n";
		for (Section thisSection : sections) {
			returnString += thisSection + "\n";
		}
		return returnString;
	}
}
