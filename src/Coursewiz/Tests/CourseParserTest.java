package Coursewiz.Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import api.schedule.CourseParser;
import api.schedule;

public class CourseParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		CourseParser parser;
		
		parser = new CourseParser(" csci 201");
		assertEquals("CSCI", parser.departmentAbbreviation);
		assertEquals(201, parser.number);
		
		parser = new CourseParser("CSCI-201");
		assertEquals("CSCI", parser.departmentAbbreviation);
		assertEquals(201, parser.number);
		
		parser = new CourseParser("ee  101");
		assertEquals("EE", parser.departmentAbbreviation);
		assertEquals(101, parser.number);
	}

}
