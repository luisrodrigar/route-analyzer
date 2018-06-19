package com.routeanalyzer.logic;

import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

public interface LapsUtils {
	
	public Lap joinLaps(Lap lapLeft, Lap lapRight);

	public void calculateLapValues(Lap lap);

	public void resetAggregateValues(Lap lap);

	public void setTotalValuesLap(Lap lap);

	public void resetTotals(Lap lap);

	/**
	 * Calculate altitude if it does not exist in each lap's track points.
	 * 
	 */
	public void calculateAltitude(Lap lap);

	public void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackpoint);

	public void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackpoint);

	/**
	 * 
	 * @param lap
	 * @param position
	 * @param timeInMillis
	 * @param index
	 * @return
	 */
	public boolean fulfillCriteriaPositionTime(Lap lap, Position position, Long timeInMillis, Integer index);

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 * 
	 * @param act:
	 *            activity
	 * @param indexLap:
	 *            index of the lap which contains track points
	 * @param lat:
	 *            latitude position
	 * @param lng:
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	public int indexOfTrackpoint(Activity activity, Integer indexLap, Position position, Long time,
			Integer index);

	public void calculateAggregateValuesLap(Lap lap);

	/**
	 * 
	 * @param initIndex
	 *            track point included
	 * @param endIndex
	 *            track point not included
	 */
	public void createSplittLap(Lap lap, Lap newLap, int initIndex, int endIndex);

}
