package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.TrackPoint;

public interface LapsOperations {

	/**
	 * Join two laps, loading the previous information and generation a new one.
	 * @param lapLeft lap with the track list located first
	 * @param lapRight lap with the track list located last
	 * @return joined lap
	 */
	Lap joinLaps(Lap lapLeft, Lap lapRight);

	/**
	 * Getting the track point regarding a lap, a position, a time millis and the
	 * index which was created (not the one regarding to the position in the track list array).
	 * @param lap
	 * @param latitude
	 * @param longitude
	 * @param time
	 * @param index
	 * @return track point with the conditions defined in the params
	 */
	TrackPoint getTrackPoint(final Lap lap, final String latitude, final String longitude, final Long time,
							 final Integer index);

	/**
	 * Split the lap from the init index of track list to end index of track list.
	 * @param lap
	 *            original lap
	 * @param initTrackPointIndex
	 *            track point included
	 * @param endTrackPointIndex
	 *            track point not included
	 * @return newLap
	 *            split lap
	 */
	Lap createSplitLap(Lap lap, int initTrackPointIndex, int endTrackPointIndex, int lapNewIndex);

	/**
	 * Exists any track point in the lap with the information defined in the params
	 * @param lap
	 * @param latitude
	 * @param longitude
	 * @param timeInMillis
	 * @param index
	 * @return true or false (exists or not)
	 */
	boolean fulfillCriteriaPositionTime(Lap lap, String latitude, String longitude, Long timeInMillis, Integer index);

	/**
	 * Calculate altitude (if it does not exist in track points) and aggregate values.
	 * @param lap
	 */
	void calculateLapValues(Lap lap);

	/**
	 * Aggregate values to null: maximum and average heart rate / speed
	 * @param lap
	 */
	void resetAggregateValues(Lap lap);

	/**
	 * Se total time seconds and distance in the lap defined in the param.
	 * @param lap
	 */
	void setTotalValuesLap(Lap lap);

	/**
	 * Total values ( distance and time ) to null.
	 * @param lap
	 */
	void resetTotals(Lap lap);

	/**
	 * Calculate altitude if it does not exist in each lap's track points.
	 * @param lap
	 */
	void calculateAltitude(Lap lap);

	/**
	 * Calculate distance between every two positions of the lap's track point list.
	 * @param lap
	 * @param previousLapLastTrackPoint null if is the first element of the first lap.
	 */
	void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackPoint);

	/**
	 * Calculate speed between every two positions of the lap's track point list.
	 * @param lap
	 * @param previousLapLastTrackPoint null if is the first element of the first lap.
	 */
	void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackPoint);

	/**
	 * Calculate all aggregate values (max and avg of heart rate and speed)
	 * @param lap
	 */
	void calculateAggregateValuesLap(Lap lap);

	/**
	 * Applied the colors to the activity's lap
	 * @param lap lap to apply the colors
	 * @param dataColor light and regular color
	 * @return Lap with the colors applied
	 */
	Lap setColorLap(Lap lap, String dataColor);

}
