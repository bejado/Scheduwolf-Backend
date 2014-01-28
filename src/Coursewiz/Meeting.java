package Coursewiz;

import java.util.ArrayList;
import java.util.List;

/**
 * A Meeting represents a time period and certain days of a week.
 * @author bdoherty
 *
 */
public class Meeting extends Object implements Choice {
	TimePeriod timePeriod = null;
	byte days = 0;
	
	public final static byte SATURDAY 	= 	1 << 0;
	public final static byte FRIDAY 	= 	1 << 1;
	public final static byte THURSDAY 	= 	1 << 2;
	public final static byte WEDNESDAY  =	1 << 3;
	public final static byte TUESDAY	=	1 << 4;
	public final static byte MONDAY		=	1 << 5;
	public final static byte SUNDAY		=	1 << 6;
	
	public final static byte ALL_DAYS	= 	SATURDAY | FRIDAY | THURSDAY | WEDNESDAY |
											TUESDAY | MONDAY | SUNDAY;
	public final static byte WEEK_DAYS	=	FRIDAY | THURSDAY | WEDNESDAY | TUESDAY | MONDAY;
	
	public Meeting(TimePeriod tp, byte d) {
		timePeriod = tp;
		days = d;
	}
	
	// Convenience constructor
	public Meeting(int startH, int startM, int endH, int endM, byte days) {
		this(new TimePeriod(startH, startM, endH, endM), days);
	}
	
	// Parser constructor
	public Meeting(String day, String startTime, String endTime) {
		// Parse the days
		byte newDays = 0;
		if (day.indexOf("M") != -1) newDays = (byte) (newDays | Meeting.MONDAY);
		if (day.indexOf("T") != -1) newDays = (byte) (newDays | Meeting.TUESDAY);
		if (day.indexOf("W") != -1) newDays = (byte) (newDays | Meeting.WEDNESDAY);
		if (day.indexOf("H") != -1) newDays = (byte) (newDays | Meeting.THURSDAY);
		if (day.indexOf("F") != -1) newDays = (byte) (newDays | Meeting.FRIDAY);
		
		// Parse the times
		TimePeriod newTimePeriod = null;
		if (startTime.indexOf(':') > 0 &&
			endTime.indexOf(':') > 0) {
			
			String [] startTimeSplit = startTime.split(":");
			String [] endTimeSplit = endTime.split(":");
			String sth = startTimeSplit[0];
			String stm = startTimeSplit[1];
			String eth = endTimeSplit[0];
			String etm = endTimeSplit[1];
			int startH = Integer.parseInt(sth);
			int startM = Integer.parseInt(stm);
			int endH = Integer.parseInt(eth);
			int endM = Integer.parseInt(etm);
			newTimePeriod = new TimePeriod(startH, startM, endH, endM);
			
		} else {
			
			newTimePeriod = new TimePeriod(24, 24, 24, 24);
			
		}
		
		timePeriod = newTimePeriod;
		days = newDays;
	}

	public List<Integer> serializeDays() {
		List<Integer> daysList = new ArrayList<Integer>();
		daysList.add( (byte)(days & Meeting.MONDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.TUESDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.WEDNESDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.THURSDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.FRIDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.SATURDAY) != 0 ? 1 : 0 );
		daysList.add( (byte)(days & Meeting.SUNDAY) != 0 ? 1 : 0 );
		return daysList;
	}
	
	public List<Integer> serializeStartTime() {
		List<Integer> startTime = new ArrayList<Integer>();
		startTime.add(timePeriod.startHour);
		startTime.add(timePeriod.startMinute);
		return startTime;
	}
	
	public List<Integer> serializeEndTime() {
		List<Integer> endTime = new ArrayList<Integer>();
		endTime.add(timePeriod.endHour);
		endTime.add(timePeriod.endMinute);
		return endTime;
	}
	
	public Boolean worksWith(Choice other) {
		Meeting otherMeeting = (Meeting)(other);
		
		byte daysInCommon = (byte) (days & otherMeeting.days);
		
		// If the two meetings don't have any days in common, they definitely work
		if (daysInCommon == 0)
			return true;
		
		// The two meetings do have days in common. However, if the time periods don't collide, they definitely work
		if (timePeriod.worksWith(otherMeeting.timePeriod))
			return true;
		else
			return false;	// But at this point, they do collide AND the days intersect
	}
	
	public String toString() {
		String returnString = "";
		if ((days & MONDAY) != 0)
			returnString += "M";
		if ((days & TUESDAY) != 0)
			returnString += "Tu";
		if ((days & WEDNESDAY) != 0)
			returnString += "W";
		if ((days & THURSDAY) != 0)
			returnString += "Th";
		if ((days & FRIDAY) != 0)
			returnString += "F";
		if ((days & SATURDAY) != 0)
			returnString += "Sa";
		if ((days & SUNDAY) != 0)
			returnString += "Su";
		
		returnString += " ";
		
		returnString += String.format("%02d:%02d - %02d:%02d", timePeriod.startHour, timePeriod.startMinute, timePeriod.endHour, timePeriod.endMinute);
		
		return returnString;
	}
	
	public double timeOnDay(byte day) {
		if ((byte)(days & day) != 0) {
			return timePeriod.getLength();
		} else {
			return 0.0;
		}
	}
	
	public boolean meetsOnDay(byte day) {
		return ((byte)(days & day) != 0);
	}
	
}
