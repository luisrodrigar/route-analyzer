package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Activity;

public interface ActivityOperations {

	/**
	 * Remove point: - Remove lap if it is the last point of the lap - Split lap
	 * into two ones if the point is between start point and end point (not
	 * included). - Remove point if point is start or end and modify gloval
	 * values of the lap
	 * 
	 * @param act
	 *            of the activity
	 * @param lat
	 *            of the position
	 * @param lng
	 *            of the position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param indexTrackPoint:
	 *            order of creation
	 * @return activity or null if there was any error.
	 */
	Activity removePoint(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint);
	
	/**
	 * Split a lap into two laps with one track point as the divider.
	 * 
	 * @param act
	 *            of the activity
	 * @param lat
	 *            of the position
	 * @param lng
	 *            of the position
	 * @param timeInMillis
	 *            timi millis
	 * @param indexTrackPoint
	 * 			  index lap to split up
	 *            of the track point which will be the divider
	 * @return activity with the new laps.
	 */
	Activity splitLap(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint);

	/**
	 * Join two laps, the result is one lap with the mixed values
	 * @param act
	 * @param indexLap1
	 * @param indexLap2
	 * @return
	 */
	Activity joinLap(Activity act, Integer indexLap1, Integer indexLap2);

	/**
	 * Delete a lap from an activity
	 * @param act
	 * @param startTime
	 * @param indexLap
	 * @return
	 */
	Activity removeLap(Activity act, Long startTime, Integer indexLap);

	/**
	 * Calculate total distance and speed of an activity.
	 * @param activity
	 */
	void calculateDistanceSpeedValues(Activity activity);


}
