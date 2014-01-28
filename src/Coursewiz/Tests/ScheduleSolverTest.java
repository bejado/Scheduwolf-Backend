package Coursewiz.Tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import Coursewiz.Course;
import Coursewiz.Course.SectionLinkType;
import Coursewiz.Meeting;
import Coursewiz.Schedule;
import Coursewiz.ScheduleSolver;
import Coursewiz.ScheduleSolver.CourseGroup;
import Coursewiz.Section;
import Coursewiz.Section.SectionType;
import Coursewiz.Solution;

public class ScheduleSolverTest {
	Course course1 = new Course("Principles of Being Arrogant");
	Course course2 = new Course("Introduction to Indian Accents");
	Course course3 = new Course("Writing for A Few Engineers But Mostly Architects");
	
	@Before
	public void setUp() throws Exception {
		setUpCourse1();
		setUpCourse2();
	}

	@Test
	public void test() {
		course1.calculateClasses();
		assertEquals(course1.getClasses().size(), 3);
		
		course2.calculateClasses();
		assertEquals(course2.getClasses().size(), 4);
		
		ScheduleSolver solver = new ScheduleSolver();
		CourseGroup group = solver.new CourseGroup();
		group.addCourse(course1);
		group.addCourse(course2);
		solver.addCourseGroup(group);
		
		solver.solve();
		Solution solution = solver.getSolution();
		
		for (Schedule thisSchedule : solution.getSchedules()) {
			System.out.println("**********SCHEULE**********");
			System.out.println(thisSchedule);
		}

	}
	
	private void setUpCourse1() {
		Section section1 = new Section();
		section1.addMeeting(new Meeting(14, 0, 15, 0, (byte)(Meeting.MONDAY | Meeting.WEDNESDAY | Meeting.FRIDAY)));
		section1.setType(SectionType.LECTURE);
		course1.addSection(section1);
		
		Section section2 = new Section();
		section2.addMeeting(new Meeting(12, 0, 13, 0, (byte)(Meeting.TUESDAY | Meeting.THURSDAY)));
		section2.setType(SectionType.LECTURE);
		course1.addSection(section2);
		
		Section section3 = new Section();
		section3.addMeeting(new Meeting(12, 0, 13, 0, (byte)(Meeting.MONDAY)));
		section3.setType(SectionType.DISCUSSION);
		course1.addSection(section3);
		
		Section section4 = new Section();
		section4.addMeeting(new Meeting(14, 0, 15, 0, (byte)(Meeting.MONDAY)));
		section4.setType(SectionType.DISCUSSION);
		course1.addSection(section4);
		
		course1.setLinkType(SectionLinkType.ONE_OF_EACH);
	}
	
	private void setUpCourse2() {
		Section section1 = new Section();
		section1.addMeeting(new Meeting(14, 30, 15, 30, (byte)(Meeting.TUESDAY | Meeting.THURSDAY)));
		section1.setType(SectionType.LECTURE);
		course2.addSection(section1);
		
		Section section2 = new Section();
		section2.addMeeting(new Meeting(17, 0, 18, 0, (byte)(Meeting.TUESDAY | Meeting.THURSDAY)));
		section2.setType(SectionType.LECTURE);
		course2.addSection(section2);
		
		Section section3 = new Section();
		section3.addMeeting(new Meeting(12, 0, 13, 0, (byte)(Meeting.TUESDAY)));
		section3.setType(SectionType.DISCUSSION);
		course2.addSection(section3);
		
		Section section4 = new Section();
		section4.addMeeting(new Meeting(10, 0, 11, 0, (byte)(Meeting.TUESDAY)));
		section4.setType(SectionType.DISCUSSION);
		course2.addSection(section4);
		
		course2.setLinkType(SectionLinkType.ONE_OF_EACH);
	}

}
