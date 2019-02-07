package com.routeanalyzer.logic.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.routeanalyzer.common.CommonUtils;
import com.routeanalyzer.logic.TrackPointUtils;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

import static com.routeanalyzer.common.CommonUtils.toTimeMillis;

@Service
public class TrackPointUtilsImpl implements TrackPointUtils {

	// Radius of earth in meters
	private static final double EARTHS_RADIUS_METERS = 6371000.0;

	@Override
	public boolean isThisTrack(TrackPoint track, Position position, Long timeInMillis, Integer index) {
		// Position: latitude and longitude
		boolean isLat = isEqualsCoordenate(track, Position::getLatitudeDegrees,
				position.getLatitudeDegrees().doubleValue());
		boolean isLng = isEqualsCoordenate(track, Position::getLongitudeDegrees,
				position.getLongitudeDegrees().doubleValue());
		// Time Millis
		boolean isTimeMillis = isEqualsValueTrack(track, TrackPoint::getDate,
				(localDateTime) -> CommonUtils.toTimeMillis((LocalDateTime) localDateTime), timeInMillis);
		// Index
		boolean isIndex = isEqualsValueTrack(track, TrackPoint::getIndex,
				(indexParm) -> ((Integer) indexParm).longValue(), index.longValue());
		
		return isLat && isLng && (isTimeMillis || isIndex);
	}

	/**
	 * Check if a track´s attribute has a specific value
	 * @param track
	 * @param methodGetter
	 * @param transforMethod
	 * @param expectedValue
	 * @return true or false
	 */
	private boolean isEqualsValueTrack(TrackPoint track, Function<TrackPoint, Object> methodGetter,
			Function<Object, Long> transforMethod, long expectedValue) {
		return Optional.ofNullable(track).map(methodGetter).map(transforMethod).filter(Predicates.equalTo(expectedValue)).isPresent();
	}

	/**
	 * Check if a coordenate (latitude or longitude) has a specific value
	 * @param track
	 * @param coordenateGetter 
	 * @param expectedValue
	 * @return true or false
	 */
	private boolean isEqualsCoordenate(TrackPoint track, Function<Position, BigDecimal> coordenateGetter,
			double expectedValue) {
		return Optional.ofNullable(track).map(TrackPoint::getPosition).map(coordenateGetter)
				.map(BigDecimal::doubleValue).filter(Predicates.equalTo(expectedValue)).isPresent();
	}

	@Override
	public double calculateDistance(Position origin, Position end) {
		// Convert degrees to radians
		double latP1 = degrees2Radians(origin.getLatitudeDegrees()),
				lngP1 = degrees2Radians(origin.getLongitudeDegrees());
		double latP2 = degrees2Radians(end.getLatitudeDegrees()), lngP2 = degrees2Radians(end.getLongitudeDegrees());

		// Point P
		double rho1 = EARTHS_RADIUS_METERS * Math.cos(latP1);
		double z1 = EARTHS_RADIUS_METERS * Math.sin(latP1);
		double x1 = rho1 * Math.cos(lngP1);
		double y1 = rho1 * Math.sin(lngP1);

		// Point Q
		double rho2 = EARTHS_RADIUS_METERS * Math.cos(latP2);
		double z2 = EARTHS_RADIUS_METERS * Math.sin(latP2);
		double x2 = rho2 * Math.cos(lngP2);
		double y2 = rho2 * Math.sin(lngP2);

		// Dot product
		double dot = (x1 * x2 + y1 * y2 + z1 * z2);
		double cosTheta = dot / (Math.pow(EARTHS_RADIUS_METERS, 2));

		double theta = Math.acos(cosTheta);

		return EARTHS_RADIUS_METERS * theta;

	}

	@Override
	public double calculateSpeed(TrackPoint origin, TrackPoint end) {
		boolean isValuesInformed = !Objects.isNull(origin) && !Objects.isNull(end) && !Objects.isNull(origin.getDate())
				&& !Objects.isNull(end.getDate());
		if (isValuesInformed) {
			long initTime = toTimeMillis(origin.getDate());
			long endTime = toTimeMillis(end.getDate());
			double totalTime = (endTime - initTime) / 1000 ;
			if (totalTime > 0)
				return Math.abs(calculateDistance(origin.getPosition(), end.getPosition())) / totalTime;
		}
		return 0.0;
	}

	/**
	 * Convert degrees to radians
	 * 
	 * @param degrees
	 *            to convert
	 * @return radians
	 */
	private static double degrees2Radians(BigDecimal degrees) {
		return degrees.doubleValue() * Math.PI / 180.0;
	}
}
