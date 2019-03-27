package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
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
	 * @param position
	 * @param time
	 * @param index
	 * @return track point with the conditions defined in the params
	 */
	TrackPoint getTrackPoint(Lap lap, Position position, Long time, Integer index);

	/**
	 * Split the lap from the init index of track list to end index of track list.
	 * @param lap
	 *            original lap
	 * @param newLap
	 *            split lap (works as a returned lap)
	 * @param initIndex
	 *            track point included
	 * @param endIndex
	 *            track point not included
	 */
	void createSplitLap(Lap lap, Lap newLap, int initIndex, int endIndex);

	/**
	 * Exists any track point in the lap with the information defined in the params
	 * @param lap
	 * @param position
	 * @param timeInMillis
	 * @param index
	 * @return true or false (exists or not)
	 */
	boolean fulfillCriteriaPositionTime(Lap lap, Position position, Long timeInMillis, Integer index);

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

}
