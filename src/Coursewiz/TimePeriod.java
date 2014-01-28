package Coursewiz;

/**
 * A duration of time, agnostic to day of the week.
 * @author bdoherty
 *
 */
public class TimePeriod implements Choice {
	int startHour;
	int startMinute;
	int endHour;
	int endMinute;
	
	/**
	 * Constructs a TimePeriod from startH : startM to endH : endM
	 * @param startH The start hour.
	 * @param startM The start minute.
	 * @param endH The end hour.
	 * @param endM The end minute.
	 */
	public TimePeriod(int startH, int startM, int endH, int endM) {
		startHour = startH;
		startMinute = startM;
		endHour = endH;
		endMinute = endM;
	}
	
	public Boolean isBefore(TimePeriod other) {
		if (endHour < other.startHour)
			return true;
		else if (endHour == other.startHour && endMinute <= other.startMinute)
			return true;
		else
			return false;
	}
	
	public Boolean isAfter(TimePeriod other) {
		if (startHour > other.endHour)
			return true;
		else if (startHour == other.endHour && startMinute >= other.endMinute)
			return true;
		else
			return false;
	}
	
	public Boolean worksWith(Choice other) {
		TimePeriod otherTimePeriod = (TimePeriod)(other);
		return this.isBefore(otherTimePeriod) || this.isAfter(otherTimePeriod);
	}
	
	/**
	 * Returns the length of this time period in hours.
	 */
	public double getLength() {
		double startTime = startHour + (startMinute / 60.0);
		double endTime = endHour + (endMinute / 60.0);
		return endTime - startTime;
	}
	
	public double getDecimalStartTime() {
		return startHour + (startMinute / 60.0);
	}
}
