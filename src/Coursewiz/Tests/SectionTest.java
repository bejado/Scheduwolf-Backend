package Coursewiz.Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Coursewiz.Meeting;
import Coursewiz.Section;

public class SectionTest {

	Section section1, section2, section3;
	
	@Before
	public void setUp() throws Exception {
		section1 = new Section();
		section1.addMeeting(new Meeting(8, 0, 9, 50, (byte)(Meeting.MONDAY | Meeting.WEDNESDAY)));
		section1.addMeeting(new Meeting(9, 0, 10, 0, (byte)(Meeting.FRIDAY)));
		
		section2 = new Section();
		section2.addMeeting(new Meeting(10, 0, 11, 50, (byte)(Meeting.TUESDAY | Meeting.THURSDAY)));
		
		section3 = new Section();
		section3.addMeeting(new Meeting(8, 30, 10, 0, (byte)(Meeting.WEDNESDAY)));
	}

	@Test
	public void test() {
		assertTrue(section1.worksWith(section2));
		assertFalse(section1.worksWith(section3));
	}

}
