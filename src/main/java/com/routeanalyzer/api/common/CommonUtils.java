package com.routeanalyzer.api.common;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.DateUtils.toZonedDateTime;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class CommonUtils {

	public static <T> T toValueOrNull(String object, Function<String, T> convertTo) {
		return ofNullable(object)
				.map(convertTo)
				.orElse(null);
	}

	public static <T> T getFirstElement(List<T> list) {
		return ofNullable(list)
				.map(listParam -> listParam.get(0))
				.orElse(null);
	}

	// Model operations

	// Methods which generate a track point.

	public static TrackPoint toTrackPoint(ZonedDateTime dateTime, int index, Position position, BigDecimal alt) {
		return TrackPoint.builder()
				.date(dateTime)
				.index(index)
				.position(position)
				.altitudeMeters(alt)
				.distanceMeters(null)
				.speed(null)
				.heartRateBpm(null)
				.build();
	}

	public static TrackPoint toTrackPoint(ZonedDateTime dateTime, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return TrackPoint.builder()
				.date(dateTime)
				.index(index)
				.position(position)
				.altitudeMeters(toBigDecimal(alt))
				.distanceMeters(toBigDecimal(dist))
				.speed(toBigDecimal(speed))
				.heartRateBpm(heartRate)
				.build();
	}

	public static TrackPoint toTrackPoint(ZonedDateTime dateTime, int index, Position position, Double alt, Double dist,
										  Double speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, position, toStringValue(alt), toStringValue(dist),
				toStringValue(speed), heartRate);
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar gregorianCalendar, int index, Position position,
										  String alt, String dist, String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toZonedDateTime(gregorianCalendar).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return toTrackPoint(toZonedDateTime(timeMillis).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(ZonedDateTime dateTime, int index, String lat, String lng, String alt,
										  String dist, String speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, toPosition(lat, lng), alt, dist, speed, heartRate);
	}
	
	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, Integer heartRate) {
		return toTrackPoint(toZonedDateTime(timeMillis).orElse(null),
				index, lat, lng, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(heartRate)
				.map(heartRateXml ->
						toTrackPoint(toZonedDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
								Integer.valueOf(heartRateXml.getValue())))
				.orElse(toTrackPoint(toZonedDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
						null));
	}

	public static TrackPoint toTrackPoint(TrackpointT trackpointT, int indexTrackPoint) {
		return toTrackPoint(trackpointT.getTime(), indexTrackPoint, trackpointT.getPosition(),
				trackpointT.getAltitudeMeters(), trackpointT.getDistanceMeters(), trackpointT.getHeartRateBpm());
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar xmlGregorianCalendar, int index, PositionT position,
										  Double alt, Double dist, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(position)
				.map(positionXml ->
						ofNullable(xmlGregorianCalendar)
								.flatMap(DateUtils::toZonedDateTime)
								.map(localDateTime ->
										ofNullable(heartRate).map(heartRateXml -> toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, Integer.valueOf(heartRateXml.getValue())))
											.orElse(toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, null))
							).orElse(null)
				).orElse(null);
	}

	// Methods which generate a position.

	public static Position toPosition(String latParam, String lngParam) {
		return toPosition(toBigDecimal(latParam), toBigDecimal(lngParam)).orElse(null);
	}

	public static Optional<Position> toOptPosition(String latParam, String lngParam) {
		return toOptPosition(toBigDecimal(latParam), toBigDecimal(lngParam));
	}

	public static Position toPosition(Double lat, Double lng) {
		return toPosition(toStringValue(lat), toStringValue(lng));
	}

	public static Optional<Position> toPosition(WptType wptType) {
		return ofNullable(wptType)
				.flatMap(__ -> toPosition(wptType.getLat(), wptType.getLon()));
	}

	public static Optional<Position> toPosition(BigDecimal latParam, BigDecimal lngParam) {
		return ofNullable(latParam)
				.flatMap(latitude -> ofNullable(lngParam)
						.map(longitude -> new Position(latitude, longitude)));
	}

	private static Optional<Position> toOptPosition(BigDecimal latParam, BigDecimal lngParam) {
		return ofNullable(latParam)
				.flatMap(latitude -> ofNullable(lngParam)
						.map(longitude -> new Position(latitude, longitude)));
	}


	// Response utils

	// Headers

	public static ResponseEntity<String> createOKApplicationOctetResponse(String file) {
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(file);
	}


	// Response Utils

	// String utils

	public static <T> List<T> toListOfType(List<String> listStrings, Function<String, T> convertTo) {
		Function<String, T> checkConvertTo = str ->  Try.of(() -> convertTo.apply(str))
				.getOrElseThrow(() -> new IllegalArgumentException(format("Could not be " +
						"convert string: %s to type", str)));
		return IntStream.range(0, listStrings.size())
				.boxed()
				.map(index -> checkConvertTo.apply(listStrings.get(index)))
				.collect(toList());
	}

	private static String toStringValue(Object value) {
		return ofNullable(value).map(String::valueOf).orElse(null);
	}

	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

}
