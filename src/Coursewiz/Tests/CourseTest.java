package Coursewiz.Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Coursewiz.Course;
import Coursewiz.Meeting;
import Coursewiz.Section;
import Coursewiz.Section.SectionType;

public class CourseTest {

	Section section1, section2, section3;
	
	@Before
	public void setUp() throws Exception {
		section1 = new Section(); section1.setType(SectionType.DISCUSSION);
		section2 = new Section(); section2.setType(SectionType.DISCUSSION);
		section3 = new Section(); section3.setType(SectionType.LECTURE);
	}

	@Test
	public void test() {
		Course course = new Course();
		course.addSection(section1);
		course.addSection(section2);
		course.addSection(section3);
		
		course.calculateClasses();
	}

}
