package com.routeanalyzer.api.common;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import lombok.experimental.UtilityClass;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@UtilityClass
public class CommonUtils {

	// Radius of earth in meters
	public static final double EARTHS_RADIUS_METERS = 6371000.0;

	/**
	 * Json Parser
	 */
	
	public static Gson getGsonLocalDateTime() {
		return new GsonBuilder().setPrettyPrinting().serializeNulls()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter()).create();
	}

	public static double metersBetweenCoordinates(double latP1, double lngP1, double latP2, double lngP2) {
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

	/**
	 * Model operations
	 */

	/**
	 *
	 * Methods which generate a track point.
	 *
	 */

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return TrackPoint.builder()
				.date(dateTime)
				.index(index)
				.position(position)
				.altitudeMeters(MathUtils.toBigDecimal(alt).orElse(null))
				.distanceMeters(MathUtils.toBigDecimal(dist).orElse(null))
				.speed(MathUtils.toBigDecimal(speed).orElse(null))
				.heartRateBpm(heartRate)
				.build();
	}

	public static String toStringValue(Object value) {
		return ofNullable(value).map(String::valueOf).orElse(null);
	}

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, Position position, Double alt, Double dist,
										  Double speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, position, toStringValue(alt), toStringValue(dist),
				toStringValue(speed), heartRate);
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar gregorianCalendar, int index, Position position,
										  String alt, String dist, String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(gregorianCalendar).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, String lat, String lng, String alt,
										  String dist, String speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, toPosition(lat, lng), alt, dist, speed, heartRate);
	}
	
	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null),
				index, lat, lng, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(heartRate)
				.map(heartRateXml ->
						toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
								new Integer(heartRateXml.getValue())))
				.orElse(toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
						null));
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar xmlGregorianCalendar, int index, PositionT position,
										  Double alt, Double dist, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(position)
				.map(positionXml ->
						ofNullable(xmlGregorianCalendar)
								.flatMap(DateUtils::toLocalDateTime)
								.map(localDateTime ->
										ofNullable(heartRate).map(heartRateXml -> toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, new Integer(heartRateXml.getValue())))
											.orElse(toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, null))
							).orElse(null)
				).orElse(null);
	}

	/**
	 *
	 * Methods which generate a position.
	 *
	 */

	public static Position toPosition(String latParam, String lngParam) {
		return toPosition(MathUtils.toBigDecimal(latParam).orElse(null), MathUtils.toBigDecimal(lngParam).orElse(null));
	}

	public static Position toPosition(Double lat, Double lng) {
		return toPosition(toStringValue(lat), toStringValue(lng));
	}

	public static Position toPosition(BigDecimal latParam, BigDecimal lngParam) {
		return ofNullable(latParam)
				.flatMap(latitude -> ofNullable(lngParam)
						.map(longitude -> new Position(latitude, longitude)))
				.orElse(null);
	}

	public static String toCoordinates(Position position, Function<Position, BigDecimal> getSpecificCoordinate) {
		return ofNullable(position)
				.map(getSpecificCoordinate)
				.map(BigDecimal::doubleValue)
				.map(String::valueOf)
				.orElse(null);
	}

}
