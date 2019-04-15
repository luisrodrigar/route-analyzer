package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;

public interface TrackPointOperations {

	/**
	 * Check if the track point corresponds with the values of the params
	 * 
	 * @param track
	 *            point
	 * @param position:
	 *            latitude degrees
	 *            longitude degrees
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index
	 * @return true or false
	 */
	boolean isThisTrack(TrackPoint track, Position position, Long timeInMillis, Integer index);

	/**
	 * Distance between two track points.
	 *
	 * @param origin
	 *            position
	 * @param end
	 *            position
	 * @return distance in meters
	 */
	Double calculateDistance(TrackPoint origin, TrackPoint end);

	/**
	 * Speed calculated with the distance information of both track points.
	 * It is a mandatory that every track point has distance attribute informed.
	 * 
	 * @param origin
	 * @param end
	 * @return speed meters per second
	 */
	Double calculateSpeed(TrackPoint origin, TrackPoint end);
	
}
