package com.routeanalyzer.api.logic.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.routeanalyzer.api.common.DateUtils.toTimeMillis;
import static java.util.Optional.ofNullable;

@Service
public class TrackPointOperationsImpl implements TrackPointOperations {

	private PositionOperations positionUtils;

	@Autowired
	public TrackPointOperationsImpl(PositionOperations positionUtils) {
		this.positionUtils = positionUtils;
	}

	@Override
	public boolean isThisTrack(TrackPoint track, Position position, Long timeInMillis, Integer index) {
		// Position: latitude and longitude
		boolean isPosition = ofNullable(track)
				.map(TrackPoint::getPosition)
				.flatMap(positionParam -> ofNullable(position)
						.map(Position::getLatitudeDegrees)
						.flatMap(latitudeDegrees -> ofNullable(position)
								.map(Position::getLongitudeDegrees)
								.map(longitudeDegrees ->
										positionUtils.isThisPosition(positionParam, latitudeDegrees, longitudeDegrees)))
				).orElse(false);
		// Time Millis
		boolean isTimeMillis = isEqualsValueTrack(track, TrackPoint::getDate,
				(localDateTime) -> toTimeMillis(LocalDateTime.class.cast(localDateTime)).orElse(null), timeInMillis);
		// Index
		boolean isIndex = isEqualsValueTrack(track, TrackPoint::getIndex,
				(indexParam) -> ((Integer) indexParam).longValue(), index.longValue());
		
		return isPosition && (isTimeMillis || isIndex);
	}

	@Override
	public Double calculateDistance(TrackPoint origin, TrackPoint end) {
		// Check track point positions are informed
		return ofNullable(origin)
				.map(TrackPoint::getPosition)
				.flatMap(originPosition -> ofNullable(end)
						.map(TrackPoint::getPosition)
						.map(endPosition -> positionUtils.calculateDistance(originPosition, endPosition)))
				.orElse(null);
	}

	@Override
	public Double calculateSpeed(TrackPoint origin, TrackPoint end) {
		return ofNullable(origin)
				.flatMap(originTrackPoint -> ofNullable(end)
						.map(endTrackPoint -> this.calculateTime(originTrackPoint, endTrackPoint))
						.filter(MathUtils::isPositiveNonZero)
						.flatMap(totalTime -> ofNullable(this.calculateDistance(originTrackPoint, end))
								.map(Math::abs)
								.map(distance -> distance / totalTime)))
				.orElse(null);
	}

	private Long calculateTime(TrackPoint origin, TrackPoint end) {
		return ofNullable(origin)
				.map(TrackPoint::getDate)
				.flatMap(DateUtils::toTimeMillis)
				.flatMap(initTime -> ofNullable(end)
						.map(TrackPoint::getDate)
						.flatMap(DateUtils::toTimeMillis)
						.map(endTime -> endTime - initTime)
						.map(DateUtils::millisToSeconds))
				.map(Double::longValue)
				.orElse(null);
	}

	/**
	 * Check if a track´s attribute has a specific value
	 * @param track
	 * @param methodGetter
	 * @param transformMethod
	 * @param expectedValue
	 * @return true or false
	 */
	private boolean isEqualsValueTrack(TrackPoint track, Function<TrackPoint, Object> methodGetter,
									   Function<Object, Long> transformMethod, long expectedValue) {
		return ofNullable(track)
				.map(methodGetter)
				.map(transformMethod)
				.filter(Predicates.equalTo(expectedValue)).isPresent();
	}

}
