package com.routeanalyzer.logic;

import java.math.BigDecimal;
import java.util.Objects;

import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

public class TrackPointsUtils {

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
	public static boolean isThisTrack(TrackPoint track, String lat, String lng, Long timeInMillis, Integer index) {
		boolean isTrack = track.getPosition() != null && track.getPosition().getLatitudeDegrees().equals(lat)
				&& track.getPosition().getLongitudeDegrees().equals(lng)
				&& ((timeInMillis != null && track.getDate().getTime() == timeInMillis)
						|| (index != null && track.getIndex() == index));
		return isTrack;
	}

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
	public static double getDistanceBetweenPoints(Position origin, Position end) {
		// Convert degrees to radians
		double latP1 = degrees2Radians(origin.getLatitudeDegrees()),
				lngP1 = degrees2Radians(origin.getLongitudeDegrees());
		double latP2 = degrees2Radians(end.getLatitudeDegrees()), lngP2 = degrees2Radians(end.getLongitudeDegrees());

		// Radius of earth in meters
		double earthRadiusMeters = 6378100.0;

		// Point P
		double rho1 = earthRadiusMeters * Math.cos(latP1);
		double z1 = earthRadiusMeters * Math.sin(latP1);
		double x1 = rho1 * Math.cos(lngP1);
		double y1 = rho1 * Math.sin(lngP1);

		// Point Q
		double rho2 = earthRadiusMeters * Math.cos(latP2);
		double z2 = earthRadiusMeters * Math.sin(latP2);
		double x2 = rho2 * Math.cos(lngP2);
		double y2 = rho2 * Math.sin(lngP2);

		// Dot product
		double dot = (x1 * x2 + y1 * y2 + z1 * z2);
		double cosTheta = dot / (Math.pow(earthRadiusMeters, 2));

		double theta = Math.acos(cosTheta);

		return earthRadiusMeters * theta;

	}

	public static void calculateSpeedBetweenPoints(TrackPoint origin, TrackPoint end) {
		double distance = getDistanceBetweenPoints(origin.getPosition(), end.getPosition());
		double time = (end.getDate().getTime() - origin.getDate().getTime()) / 1000;
		double speed = 0.0;
		if (time > 0)
			speed = distance / time;
		if (Objects.isNull(end.getDistanceMeters())
				|| (!Objects.isNull(end.getDistanceMeters()) && end.getDistanceMeters().doubleValue() != distance))
			end.setDistanceMeters(new BigDecimal(distance));
		end.setSpeed(new BigDecimal(speed));
	}

	private static double degrees2Radians(String degrees) {
		return Double.parseDouble(degrees) * Math.PI / 180.0;
	}
}
