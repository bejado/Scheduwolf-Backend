package Coursewiz.Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Coursewiz.TimePeriod;
import Coursewiz.Meeting;
import java.util.List;
import java.util.ArrayList;

public class MeetingTest {
	
	List<Meeting> meetings = new ArrayList<Meeting>();

	@Before
	public void setUp() throws Exception {
		meetings.add( new Meeting(12, 0, 13, 50, (byte) (Meeting.MONDAY | Meeting.WEDNESDAY)) );	// 12:00 - 1:50 pm Monday Wednesday
		meetings.add( new Meeting(14, 0, 14, 50, (byte) (Meeting.WEDNESDAY)) );						// 2:00 - 2:50 pm Wednesday
		meetings.add( new Meeting(11, 0, 12, 30, (byte) (Meeting.MONDAY | Meeting.TUESDAY)) );		// 11:00 - 12:30 pm Monday Tuesday
		meetings.add( new Meeting(14, 30, 16, 0, (byte) (Meeting.THURSDAY)) );						// 2:30 - 4:00 pm Thursday
		meetings.add( new Meeting(10, 00, 11, 15, (byte) (Meeting.TUESDAY)) );						// 10:00 - 11:15 am Tuesday
		meetings.add( new Meeting(9, 00, 10, 00, (byte) (Meeting.TUESDAY)) );						// 9:00 - 10:00 am Tuesday
	}

	@Test
	public void test() {
		assertTrue(meetings.get(2).worksWith(meetings.get(1)));
		assertTrue(meetings.get(1).worksWith(meetings.get(2)));
		assertTrue(meetings.get(1).worksWith(meetings.get(3)));
		assertTrue(meetings.get(3).worksWith(meetings.get(1)));
		
		assertTrue(meetings.get(0).worksWith(meetings.get(1)));
		assertTrue(meetings.get(1).worksWith(meetings.get(0)));
		assertFalse(meetings.get(4).worksWith(meetings.get(2)));
		assertFalse(meetings.get(2).worksWith(meetings.get(4)));
		
		assertTrue(meetings.get(4).worksWith(meetings.get(5)));
	}

}
