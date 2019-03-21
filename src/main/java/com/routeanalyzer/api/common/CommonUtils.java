package com.routeanalyzer.common;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.xml.tcx.PositionT;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

@UtilityClass
public class CommonUtils {

	// Radius of earth in meters
	public static final double EARTHS_RADIUS_METERS = 6371000.0;

	/**
	 * Date Time operations
	 */
	public static Optional<Date> toDate(LocalDateTime localDateTime) {
		return getInstant(localDateTime)
				.map(Date::from);
	}
	
	public static Optional<Long> toTimeMillis(LocalDateTime localDateTime) {
		return getInstant(localDateTime)
				.map(Instant::toEpochMilli);
	}

	private static Optional<Instant> getInstant(LocalDateTime localDateTime) {
		return ofNullable(localDateTime)
				.map(dateTime -> dateTime.atZone(ZoneId.systemDefault()))
				.map(ZonedDateTime::toInstant);
	}
	
	public static Optional<LocalDateTime> toLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return ofNullable(xmlGregorianCalendar)
				.map(XMLGregorianCalendar::toGregorianCalendar)
				.flatMap(CommonUtils::toLocalDateTime);
	}

	public static Optional<LocalDateTime> toLocalDateTime(GregorianCalendar xmlGregorianCalendar) {
		return ofNullable(xmlGregorianCalendar)
				.map(GregorianCalendar::getTime)
				.map(Date::toInstant)
				.flatMap(CommonUtils::toLocalDateTime);
	}
	
	public static Optional<LocalDateTime> toLocalDateTime(long timeMillis) {
		return ofNullable(timeMillis)
				.map(Instant::ofEpochMilli)
				.flatMap(CommonUtils::toLocalDateTime);
	}

	public static Optional<LocalDateTime> toLocalDateTime(Instant instant) {
		return ofNullable(instant)
				.map(inst -> inst.atZone(ZoneId.systemDefault()))
				.map(ZonedDateTime::toLocalDateTime);
	}

	public static GregorianCalendar createGregorianCalendar(Date date) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		return gregorianCalendar;
	};

	public static XMLGregorianCalendar createXmlGregorianCalendar(GregorianCalendar gregorianCalendar) {
		return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
				.getOrElse(() -> null);
	}
	
	/**
	 * Json Parser
	 */
	
	public static Gson getGsonLocalDateTime() {
		return new GsonBuilder().setPrettyPrinting().serializeNulls()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter()).create();
	}
	
	/**
	 * Mathematic operations
	 */
	
	public static Optional<BigDecimal> toBigDecimal(String number) {
		return ofNullable(number).filter(StringUtils::isNotEmpty).map(BigDecimal::new);
	}

	public static BigDecimal toBigDecimal(Double number) {
		return ofNullable(number).map(BigDecimal::new).orElse(null);
	}
	
	public static double round(double number, int round) {
		double roundNumber = Math.pow(10, round);
		return Math.round(number * roundNumber) / roundNumber;
	}

	public static double millisToSeconds(double milliSeconds){
		return milliSeconds / 1000;
	}

	public static double degrees2Radians(BigDecimal degrees) {
		return degrees.doubleValue() * Math.PI / 180.0;
	}

	public static double meteersBetweenCoordinates(double latP1, double lngP1, double latP2, double lngP2) {
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
	 * Logical operations
	 */

	public static boolean isPositiveNonZero(Double value) {
		return value > 0;
	}

	public static boolean isPositiveHeartRate(HeartRateInBeatsPerMinuteT heartRate) {
		return heartRate.getValue() > 0;
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
				.altitudeMeters(toBigDecimal(alt).orElse(null))
				.distanceMeters(toBigDecimal(dist).orElse(null))
				.speed(toBigDecimal(speed).orElse(null))
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
		return toTrackPoint(toLocalDateTime(gregorianCalendar).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return toTrackPoint(toLocalDateTime(timeMillis).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, String lat, String lng, String alt,
										  String dist, String speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, toPosition(lat, lng), alt, dist, speed, heartRate);
	}
	
	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, Integer heartRate) {
		return toTrackPoint(toLocalDateTime(timeMillis).orElse(null),
				index, lat, lng, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(heartRate)
				.map(heartRateXml ->
						toTrackPoint(toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
								new Integer(heartRateXml.getValue())))
				.orElse(toTrackPoint(toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
						null));
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar xmlGregorianCalendar, int index, PositionT position,
										  Double alt, Double dist, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(position)
				.map(positionXml ->
						ofNullable(xmlGregorianCalendar)
								.flatMap(CommonUtils::toLocalDateTime)
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
		return toPosition(toBigDecimal(latParam).orElse(null), toBigDecimal(lngParam).orElse(null));
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
