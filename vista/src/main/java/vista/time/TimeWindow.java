/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.time;

/**
 * Defines a range of time. This is used to specify the range for which the data
 * may be retrieved.
 */
public interface TimeWindow extends java.io.Serializable
{
	/**
	 * create a copy of itself
	 *
	 * @return the copy
	 */
	TimeWindow create();

	/**
	 * creates a time window with the given start and end times.
	 */
	TimeWindow create(Time startTime, Time endTime);

	/**
	 * Gets the starting time for this window
	 *
	 * @return the starting time since Dec 31, 1899 Midnight
	 */
	Time getStartTime();

	/**
	 * Gets the end time for this window
	 *
	 * @return the ending time since Dec 31, 1899 Midnight
	 */
	Time getEndTime();

	/**
	 * creates a time window which is the intersection of this time window with
	 * given time window.
	 *
	 * @return new time window object representing intersection or null if no
	 * intersection is possible.
	 */
	TimeWindow intersection(TimeWindow timeWindow);

	/**
	 * true if time window intersects with the given time window
	 */
	boolean intersects(TimeWindow timeWindow);

	/**
	 * returns true if given time window is contained completely in the current
	 * time window.
	 */
	boolean contains(TimeWindow timeWindow);

	/**
	 * returns true if this time window contains the given time
	 */
	boolean contains(Time time);

	/**
	 * tests for similarity
	 */
	boolean isSameAs(TimeWindow tw);

	/**
	 * creates a time window which spans both this and the provided time window
	 *
	 * @param timeWindow
	 * @return
	 */
	TimeWindow union(TimeWindow timeWindow);
}
