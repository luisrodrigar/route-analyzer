package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;

public interface LapsUtils {
	
	Lap joinLaps(Lap lapLeft, Lap lapRight);

	void calculateLapValues(Lap lap);

	void resetAggregateValues(Lap lap);

	void setTotalValuesLap(Lap lap);

	void resetTotals(Lap lap);

	/**
	 * Calculate altitude if it does not exist in each lap's track points.
	 * 
	 */
	void calculateAltitude(Lap lap);

	void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackpoint);

	void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackpoint);

	/**
	 * 
	 * @param lap
	 * @param position
	 * @param timeInMillis
	 * @param index
	 * @return
	 */
	boolean fulfillCriteriaPositionTime(Lap lap, Position position, Long timeInMillis, Integer index);

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 * 
	 * @param activity:
	 *            activity
	 * @param indexLap:
	 *            index of the lap which contains track points
	 * @param position:
	 *            latitude position
	 *            longitude position
	 * @param time:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	int indexOfTrackpoint(Activity activity, Integer indexLap, Position position, Long time,
			Integer index);

	void calculateAggregateValuesLap(Lap lap);

	/**
	 * 
	 * @param initIndex
	 *            track point included
	 * @param endIndex
	 *            track point not included
	 */
	void createSplittLap(Lap lap, Lap newLap, int initIndex, int endIndex);

}
