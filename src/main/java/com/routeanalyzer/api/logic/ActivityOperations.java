package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Position;

import java.util.List;

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
	 * @param activity
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
	Activity splitLap(Activity activity, String lat, String lng, String timeInMillis, String indexTrackPoint);

	/**
	 * Join two laps, the result is one lap with the mixed values
	 * @param activity
	 * @param indexLeft
	 * @param indexRight
	 * @return
	 */
	Activity joinLaps(Activity activity, String indexLeft, String indexRight);

	/**
	 * Delete a lap from an activity
	 * @param act
	 * @param startTime optional (if it is not informed, just pay attention to the index lap)
	 * @param indexLap
	 * @return
	 */
	Activity removeLaps(Activity act, List<Long> startTime, List<Integer> indexLap);

	/**
	 * Calculate total distance and speed of an activity.
	 * @param activity
	 */
	void calculateDistanceSpeedValues(Activity activity);

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 *
	 * @param activity:
	 *            activity
	 * @param indexLap:
	 *            index of the lap
	 * @param position:
	 *            latitude position
	 *            longitude position
	 * @param time:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	int indexOfTrackPoint(Activity activity, Integer indexLap, Position position, Long time, Integer index);

}
