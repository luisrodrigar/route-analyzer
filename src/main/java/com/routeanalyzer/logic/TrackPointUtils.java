package com.routeanalyzer.logic;

import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

public interface TrackPointUtils {

	/**
	 * Check if the track point corresponds with the values of the params
	 * 
	 * @param track
	 *            point
	 * @param lat:
	 *            latitude degrees
	 * @param lng:
	 *            longitude degrees
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index
	 * @return true or false
	 */
	public boolean isThisTrack(TrackPoint track, Position position, Long timeInMillis, Integer index);

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
	public double getDistanceBetweenPoints(Position origin, Position end);

	public double getSpeedBetweenPoints(TrackPoint previous, TrackPoint current);

	public void calculateSpeedBetweenPoints(TrackPoint origin, TrackPoint end);
	
}
