package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;

import static com.routeanalyzer.api.common.CommonUtils.toStringValue;
import static com.routeanalyzer.api.common.DateUtils.toTimeMillis;
import static com.routeanalyzer.api.common.DateUtils.toUtcZonedDateTime;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static io.vavr.Predicates.is;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class TrackPointOperationsImpl implements TrackPointOperations {

	private final PositionOperations positionOperations;

	@Override
	public boolean isThisTrack(final TrackPoint track, final String latitude, final String longitude,
							   final Long timeInMillis, final Integer index) {
		// Position: latitude and longitude
		boolean isPosition = ofNullable(track)
				.map(TrackPoint::getPosition)
				.map(positionParam -> positionOperations.isThisPosition(positionParam, latitude, longitude))
				.orElse(false);
		// Time Millis
		boolean isTimeMillis = isEqualsValueTrack(track, TrackPoint::getDate,
				(localDateTime) -> toTimeMillis((ZonedDateTime) localDateTime).orElse(null), timeInMillis);
		// Index
		boolean isIndex = isEqualsValueTrack(track, TrackPoint::getIndex,
				(indexParam) -> ((Integer) indexParam).longValue(), index.longValue());
		
		return isPosition && (isTimeMillis || isIndex);
	}

	@Override
	public Double calculateDistance(final TrackPoint origin, final TrackPoint end) {
		// Check track point positions are informed
		return ofNullable(origin)
				.map(TrackPoint::getPosition)
				.flatMap(originPosition -> ofNullable(end)
						.map(TrackPoint::getPosition)
						.flatMap(endPosition -> positionOperations.calculateDistance(originPosition, endPosition)))
				.orElse(null);
	}

	@Override
	public Double calculateSpeed(final TrackPoint origin, final TrackPoint end) {
		return ofNullable(origin)
				.flatMap(__ -> ofNullable(end)
						.map(___ -> calculateTime(origin, end))
						.filter(MathUtils::isPositiveNonZero)
						.flatMap(totalTime -> ofNullable(calculateDistance(origin, end))
								.map(Math::abs)
								.map(distance -> distance / totalTime)))
				.orElse(null);
	}

	@Override
	public TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
								   final BigDecimal altitude) {
		return TrackPoint.builder()
				.date(zonedDateTime)
				.index(index)
				.position(position)
				.altitudeMeters(altitude)
				.distanceMeters(null)
				.speed(null)
				.heartRateBpm(null)
				.build();
	}

	@Override
	public TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
								   final String altitude, final String distance, final String speed,
								   final Integer heartRate) {
		return TrackPoint.builder()
				.date(zonedDateTime)
				.index(index)
				.position(position)
				.altitudeMeters(toBigDecimal(altitude))
				.distanceMeters(toBigDecimal(distance))
				.speed(toBigDecimal(speed))
				.heartRateBpm(heartRate)
				.build();
	}

	@Override
	public TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
								   final Double altitude, final Double distance, final Double speed,
								   final Integer heartRate) {
		return toTrackPoint(zonedDateTime, index, position, toStringValue(altitude), toStringValue(distance),
				toStringValue(speed), heartRate);
	}

	@Override
	public TrackPoint toTrackPoint(final XMLGregorianCalendar xmlGregorianCalendar, final int index,
								   final Position position, final String altitude, final String distance,
								   final String speed, final Integer heartRate) {
		return toUtcZonedDateTime(xmlGregorianCalendar)
				.map(zonedDateTime -> toTrackPoint(zonedDateTime, index, position, altitude, distance, speed, heartRate))
				.orElse(null);
	}

	@Override
	public TrackPoint toTrackPoint(final long timeMillis, final int index, final Position position,
								   final String altitude, final String distance, final String speed,
								   final Integer heartRate) {
		return toUtcZonedDateTime(timeMillis)
				.map(zonedDateTime -> toTrackPoint(zonedDateTime, index, position, altitude, distance, speed, heartRate))
				.orElse(null);
	}

	@Override
	public TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final String latitude,
								   final String longitude, final String altitude, final String distance,
								   final String speed, final Integer heartRate) {
		return toTrackPoint(zonedDateTime, index, positionOperations.toPosition(latitude, longitude), altitude,
				distance, speed, heartRate);
	}

	@Override
	public TrackPoint toTrackPoint(final long timeMillis, final int index, final String latitude,
								   final String longitude, final String altitude, final String distance,
								   final String speed, final Integer heartRate) {
		return toUtcZonedDateTime(timeMillis)
				.map(zonedDateTime -> toTrackPoint(zonedDateTime, index, latitude, longitude, altitude, distance, speed, heartRate))
				.orElse(null);
	}

	@Override
	public TrackPoint toTrackPoint(final TrackpointT trackpointT, final int index) {
		return positionOperations.toPosition(trackpointT)
				.filter(__ -> nonNull(trackpointT.getTime()))
				.map(position -> ofNullable(trackpointT)
						.map(TrackpointT::getHeartRateBpm)
						.map(HeartRateInBeatsPerMinuteT::getValue)
						.map(Integer::valueOf)
						.map(hearRate -> toTrackPoint(trackpointT.getTime(), index, position,
								toStringValue(trackpointT.getAltitudeMeters()),
								toStringValue(trackpointT.getDistanceMeters()), null, hearRate))
						.orElseGet(() -> toTrackPoint(trackpointT.getTime(), index, position,
								toStringValue(trackpointT.getAltitudeMeters()),
								toStringValue(trackpointT.getDistanceMeters()), null, null))
				).orElse(null);
	}

	@Override
	public Optional<TrackPoint> toTrackPoint(final WptType wptType, final int index) {
		return positionOperations.toPosition(wptType)
				.filter(__ -> nonNull(wptType.getTime()))
				.map(position -> toTrackPoint(wptType.getTime(), index, position, toStringValue(wptType.getEle()),
						null, null, null));
	}

	private Double calculateTime(final TrackPoint origin, final TrackPoint end) {
		return ofNullable(origin)
				.map(TrackPoint::getDate)
				.flatMap(DateUtils::toTimeMillis)
				.flatMap(initTime -> ofNullable(end)
						.map(TrackPoint::getDate)
						.flatMap(DateUtils::toTimeMillis)
						.map(endTime -> endTime - initTime)
						.map(Double::valueOf)
						.map(DateUtils::millisToSeconds))
				.orElse(null);
	}

	private boolean isEqualsValueTrack(final TrackPoint track, Function<TrackPoint, Object> methodGetter,
									   final Function<Object, Long> transformMethod, final long expectedValue) {
		return ofNullable(track)
				.map(methodGetter)
				.map(transformMethod)
				.filter(is(expectedValue))
				.isPresent();
	}

}
