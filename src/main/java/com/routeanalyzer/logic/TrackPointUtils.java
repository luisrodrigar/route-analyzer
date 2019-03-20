package com.routeanalyzer.logic;

import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

public interface TrackPointUtils {

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
	 * Distance between two positions. Degrees to radians and then, radians to
	 * meters.
	 * 
	 * @param origin
	 *            position
	 * @param end
	 *            position
	 * @return distance in meters
	 */
	double calculateDistance(Position origin, Position end);

	/**
	 * Speed calculated with the distance information of both track points.
	 * It is a mandatory that every track point has distance attribute informed.
	 * 
	 * @param origin
	 * @param end
	 * @return speed
	 */
	double calculateSpeed(TrackPoint origin, TrackPoint end);
	
}
