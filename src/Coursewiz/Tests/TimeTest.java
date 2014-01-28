package Coursewiz.Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import Coursewiz.TimePeriod;

public class TimeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void standardTest() {
		TimePeriod earlyMeeting = new TimePeriod(6, 30, 7, 30); 	// 6:30 - 7:30 AM
		TimePeriod morningMeeting = new TimePeriod(8, 0, 9, 0);		// 8:00 - 9:00 AM
		TimePeriod lunchMeeting = new TimePeriod(11, 0, 12, 0);		// 11:00 - 12:00 PM
		
		assertTrue(earlyMeeting.isBefore(morningMeeting));
		assertTrue(morningMeeting.isAfter(earlyMeeting));
		assertTrue(earlyMeeting.isBefore(lunchMeeting));
		
		assertFalse(earlyMeeting.isAfter(lunchMeeting));
		assertFalse(lunchMeeting.isBefore(earlyMeeting));
		assertFalse(morningMeeting.isAfter(lunchMeeting));
	}
	
	@Test
	public void edgeCase() {
		TimePeriod meeting1 = new TimePeriod(10, 0, 10, 30);		// 10:00 - 10:30
		TimePeriod meeting2 = new TimePeriod(10, 30, 12, 00);		// 10:30 - 12:00
		
		assertTrue(meeting1.isBefore(meeting2));
		assertTrue(meeting2.isAfter(meeting1));
	}
	
	@Test
	public void intersectingTimePeriods() {
		TimePeriod meeting1 = new TimePeriod(4, 20, 5, 20);
		TimePeriod meeting2 = new TimePeriod(6, 0, 7, 0);
		TimePeriod meeting3 = new TimePeriod(5, 0, 6, 30);
		
		assertTrue(meeting1.worksWith(meeting2));
		assertFalse(meeting1.worksWith(meeting3));
		assertFalse(meeting2.worksWith(meeting3));
	}
	
	@Test
	public void intersectingTimePeriodEdgeCase() {
		TimePeriod meeting1 = new TimePeriod(1, 0, 2, 0);
		TimePeriod meeting2 = new TimePeriod(2, 0, 3, 0);
		
		assertTrue(meeting1.worksWith(meeting2));
		assertTrue(meeting2.worksWith(meeting1));
	}

}
