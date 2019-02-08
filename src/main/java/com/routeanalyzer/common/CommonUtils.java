package com.routeanalyzer.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonUtils {
	
	/**
	 * Local Date Time operations
	 */
	
	public Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public long toTimeMillis(LocalDateTime localDateTime) {
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	
	public LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return LocalDateTime.from(
				xmlGregorianCalendar.toGregorianCalendar().getTime().toInstant().atZone(ZoneId.systemDefault()));
	}
	
	public LocalDateTime toLocalDateTime(long timeMillis) {
		return Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	/**
	 * Json Parser
	 */
	
	public Gson getGsonLocalDateTime() {
		return new GsonBuilder()
				.setPrettyPrinting()
				.serializeNulls()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter())
				.create();
	}
	
	/**
	 * Mathematic operations
	 */
	
	public BigDecimal toBigDecimal(String number) {
		return Optional.ofNullable(number).map(BigDecimal::new).orElse(null);
	}
	
	public double round(double number, int round) {
		double roundNumber = Math.pow(10, round);
		return Math.round(number * roundNumber) / roundNumber;
	}

	/**
	 * Model operations
	 */
	
	public Position toPosition(String lat, String lng) {
		return Optional.ofNullable(lat)
				.map(latitude -> Optional.ofNullable(lng).map(longitude -> Position.builder()
						.latitudeDegrees(toBigDecimal(latitude)).longitudeDegrees(toBigDecimal(longitude)).build())
						.orElse(null))
				.orElse(null);
	}
	
	public String toCoordenate(Position position, Function<Position, BigDecimal> method) {
		return Optional.ofNullable(position)
				.map(method)
				.map(BigDecimal::doubleValue)
				.map(String::valueOf)
				.orElse(null);
	}
	
	public TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
			String speed, int heartRate) {
		return TrackPoint.builder()
				.date(toLocalDateTime(timeMillis))
				.index(index)
				.position(toPosition(lat, lng))
				.altitudeMeters(toBigDecimal(alt))
				.distanceMeters(toBigDecimal(dist))
				.speed(toBigDecimal(speed))
				.heartRateBpm(heartRate)
				.build();
	}

	public TrackPoint toTrackPointPosition(long timeMillis, int index, Position position, String alt, String dist,
			String speed, int heartRate) {
		return toTrackPoint(timeMillis, index, toCoordenate(position, Position::getLatitudeDegrees),
				toCoordenate(position, Position::getLongitudeDegrees), alt, dist,
				speed, heartRate);
	}
	

	
}
