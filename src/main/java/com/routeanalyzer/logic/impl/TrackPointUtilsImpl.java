package com.routeanalyzer.logic.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.routeanalyzer.common.CommonUtils;
import com.routeanalyzer.logic.TrackPointUtils;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import static com.routeanalyzer.common.CommonUtils.meteersBetweenCoordinates;
import static com.routeanalyzer.common.CommonUtils.toTimeMillis;
import static java.util.Optional.ofNullable;

@Service
public class TrackPointUtilsImpl implements TrackPointUtils {

	@Override
	public boolean isThisTrack(TrackPoint track, Position position, Long timeInMillis, Integer index) {
		// Position: latitude and longitude
		boolean isLat = isEqualsCoordenate(track, Position::getLatitudeDegrees,
				position.getLatitudeDegrees().doubleValue());
		boolean isLng = isEqualsCoordenate(track, Position::getLongitudeDegrees,
				position.getLongitudeDegrees().doubleValue());
		// Time Millis
		boolean isTimeMillis = isEqualsValueTrack(track, TrackPoint::getDate,
				(localDateTime) -> toTimeMillis(LocalDateTime.class.cast(localDateTime)).orElse(null), timeInMillis);
		// Index
		boolean isIndex = isEqualsValueTrack(track, TrackPoint::getIndex,
				(indexParam) -> ((Integer) indexParam).longValue(), index.longValue());
		
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
		return ofNullable(track)
				.map(methodGetter)
				.map(transforMethod)
				.filter(Predicates.equalTo(expectedValue)).isPresent();
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
		return ofNullable(track)
				.map(TrackPoint::getPosition).map(coordenateGetter)
				.map(BigDecimal::doubleValue).filter(Predicates.equalTo(expectedValue)).isPresent();
	}

	@Override
	public double calculateDistance(Position origin, Position end) {
		// Get all coordinates
		return ofNullable(origin)
				.map(Position::getLatitudeDegrees)
				.map(CommonUtils::degrees2Radians)
				.flatMap(latitudeOrigin -> ofNullable(origin)
						.map(Position::getLongitudeDegrees)
						.map(CommonUtils::degrees2Radians)
						.flatMap(longitudeOrigin -> ofNullable(end)
								.map(Position::getLatitudeDegrees)
								.map(CommonUtils::degrees2Radians)
								.flatMap(latitudeEnd -> ofNullable(end)
										.map(Position::getLongitudeDegrees)
										.map(CommonUtils::degrees2Radians)
										.map(longitudeEnd ->
												meteersBetweenCoordinates(latitudeOrigin, longitudeOrigin,
														latitudeEnd, longitudeEnd)))))
				.orElse(0.0);
	}

	@Override
	public double calculateSpeed(TrackPoint origin, TrackPoint end) {
		Predicate<Double> timeIsGreaterThanZero = (totalTime) -> totalTime > 0;
		return ofNullable(origin)
				.map(TrackPoint::getDate)
				.flatMap(CommonUtils::toTimeMillis)
				.flatMap(initTime -> ofNullable(end).map(TrackPoint::getDate)
						.flatMap(CommonUtils::toTimeMillis)
						.map(endTime -> endTime - initTime)
						.map(CommonUtils::millisToSeconds)
						.filter(timeIsGreaterThanZero)
						.map(totalTime ->
								Math.abs(calculateDistance(origin.getPosition(), end.getPosition())) / totalTime))
				.orElse(0.0);
	}

}
