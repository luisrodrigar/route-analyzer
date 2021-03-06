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
	Lap joinLaps(final Lap lapLeft, final Lap lapRight);

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
	Lap createSplitLap(final Lap lap, final int initTrackPointIndex, final int endTrackPointIndex,
					   final int lapNewIndex);

	/**
	 * Exists any track point in the lap with the information defined in the params
	 * @param lap
	 * @param latitude
	 * @param longitude
	 * @param timeInMillis
	 * @param index
	 * @return true or false (exists or not)
	 */
	boolean fulfillCriteriaPositionTime(final Lap lap, final String latitude, final String longitude,
										final Long timeInMillis, final Integer index);

	/**
	 * Calculate altitude (if it does not exist in track points) and aggregate values.
	 * @param lap
	 */
	void calculateLapValues(final Lap lap);

	/**
	 * Aggregate values to null: maximum and average heart rate / speed
	 * @param lap
	 */
	void resetAggregateValues(final Lap lap);

	/**
	 * Se total time seconds and distance in the lap defined in the param.
	 * @param lap
	 */
	void setTotalValuesLap(final Lap lap);

	/**
	 * Total values ( distance and time ) to null.
	 * @param lap
	 */
	void resetTotals(final Lap lap);

	/**
	 * Calculate altitude if it does not exist in each lap's track points.
	 * @param lap
	 */
	void calculateAltitude(final Lap lap);

	/**
	 * Calculate distance between every two positions of the lap's track point list.
	 * @param lap
	 * @param previousLapLastTrackPoint null if is the first element of the first lap.
	 */
	void calculateDistanceLap(final Lap lap, final TrackPoint previousLapLastTrackPoint);

	/**
	 * Calculate speed between every two positions of the lap's track point list.
	 * @param lap
	 * @param previousLapLastTrackPoint null if is the first element of the first lap.
	 */
	void calculateSpeedLap(final Lap lap, final TrackPoint previousLapLastTrackPoint);

	/**
	 * Calculate all aggregate values (max and avg of heart rate and speed)
	 * @param lap
	 */
	void calculateAggregateValuesLap(final Lap lap);

	/**
	 * Applied the colors to the activity's lap
	 * @param lap lap to apply the colors
	 * @param dataColor light and regular color
	 * @return Lap with the colors applied
	 */
	Lap setColorLap(final Lap lap, final String dataColor);

}
