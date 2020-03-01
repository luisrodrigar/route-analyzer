package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TrackPointOperationsImplTest {

	@Mock
	private PositionOperationsImpl positionOperations;
	@InjectMocks
	private TrackPointOperationsImpl trackPointOperations;

	private Position oviedo;
	private Position madrid;
	private Position park;
	private TrackPoint oviedoTrack;
	private TrackPoint parkTrack;
	private TrackPoint madridTrack;
	private BigDecimal latParam;
	private BigDecimal lngParam;
	private String latitude;
	private String longitude;

	@Before
	public void setUp() {
		// Given
		oviedo = new Position(new BigDecimal("43.3602900"), new BigDecimal("-5.8447600"));
		madrid = new Position(new BigDecimal("40.4165000"), new BigDecimal("-3.7025600"));
		park = new Position(new BigDecimal("43.352478"), new BigDecimal("-5.8501170"));
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 12, 0, 5, 0);
		ZoneId zoneId = ZoneId.of("UTC");
		Clock fixed = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), zoneId);
		ZonedDateTime now = ZonedDateTime.now(fixed);
		oviedoTrack = TrackPoint
				.builder()
				.date(now)
				.index(1)
				.position(oviedo)
				.altitudeMeters(new BigDecimal("100"))
				.distanceMeters(new BigDecimal("0"))
				.heartRateBpm(78)
				.build();
		parkTrack = TrackPoint
				.builder()
				.date(now.plusMinutes(15L))
				.index(2)
				.position(park)
				.altitudeMeters(new BigDecimal("100"))
				.distanceMeters(new BigDecimal("970.64"))
				.heartRateBpm(78)
				.build();
		madridTrack = TrackPoint
				.builder()
				.date(now.plusHours(6L))
				.index(3)
				.position(madrid)
				.altitudeMeters(new BigDecimal("100"))
				.distanceMeters(new BigDecimal("372247.30"))
				.heartRateBpm(78)
				.build();
		latitude = "12";
		latParam = new BigDecimal(latitude);
		longitude = "6";
		lngParam = new BigDecimal("6");
	}

	@Test
	public void isThisTrackPositionEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint
				.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();
		doReturn(true).when(positionOperations).isThisPosition(position, latitude, longitude);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12123123L, 0);

		// Then
		assertThat(isThisTrack).isTrue();
		verify(positionOperations).isThisPosition(eq(position),  eq(latitude), eq(longitude));
	}

	@Test
	public void isThisTrackPositionNullTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(null)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionLatNotEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint,"5.67","6", 12123123L, 0);

		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionLngNotEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionIndexEqualTimeMillisNotEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();

		doReturn(true).when(positionOperations).isThisPosition(position, latitude, longitude);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12L, 0);

		// Then
		assertThat(isThisTrack).isTrue();
		verify(positionOperations).isThisPosition(eq(position), eq(latitude), eq(longitude));
	}

	@Test
	public void isThisTrackPositionTimeMillisEqualIndexNotEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(0)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();
		doReturn(true).when(positionOperations).isThisPosition(position, latitude, longitude);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12123123L, 13);

		// Then
		assertThat(isThisTrack).isTrue();
		verify(positionOperations).isThisPosition(eq(position), eq(latitude), eq(longitude));
	}

	@Test
	public void isThisTrackPositionIndexTimeMillisNotEqualTest() {
		// Given
		Position position = Position.builder()
				.latitudeDegrees(latParam)
				.longitudeDegrees(lngParam)
				.build();
		TrackPoint trackpoint = TrackPoint.builder()
				.date(ZonedDateTime.ofInstant(Instant.ofEpochMilli(12123123L), ZoneId.of("UTC")))
				.index(3)
				.position(position)
				.altitudeMeters(new BigDecimal("450"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(150)
				.build();
		doReturn(true).when(positionOperations).isThisPosition(position, latitude, longitude);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, position.getLatitudeDegrees().toString(),
				position.getLongitudeDegrees().toString(), 12L, 0);

		// Then
		assertThat(isThisTrack).isFalse();
		verify(positionOperations).isThisPosition(eq(position), eq(latitude), eq(longitude));
	}

	@Test
	public void calculateSpeed() {
		// Given
		doReturn(of(18000.0)).when(positionOperations).calculateDistance(eq(oviedo), eq(park));
		doReturn(of(931500.0)).when(positionOperations).calculateDistance(eq(park), eq(madrid));

		// When
		Double speedOvdPark = trackPointOperations.calculateSpeed(oviedoTrack, parkTrack);
		Double speedParkMad = trackPointOperations.calculateSpeed(parkTrack, madridTrack);

		// Then
		assertThat(speedOvdPark).isEqualTo(20);
		assertThat(speedParkMad).isEqualTo(45);
	}

	@Test
	public void calculateSpeedSamePoint() {
		// Given
		TrackPoint oviedoTrack = TrackPoint.builder()
				.date(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")))
				.index(1)
				.position(oviedo)
				.altitudeMeters(new BigDecimal("100"))
				.distanceMeters(new BigDecimal("10"))
				.speed(null)
				.heartRateBpm(78)
				.build();
		// When 
		Double speedOvdOvd = trackPointOperations.calculateSpeed(oviedoTrack, oviedoTrack);
		// Then
		assertThat(speedOvdOvd).isNull();
	}

	@Test
	public void toTrackPointFromZonedDateTimeBasicDataTest() {
		// Given
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		BigDecimal altitude = new BigDecimal("345.34");

		// When
		TrackPoint trackPoint =
				trackPointOperations.toTrackPoint(zonedDateTime, index, position, altitude);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(altitude);
	}

	@Test
	public void toTrackPointFromZonedDateMoreDataTest() {
		// Given
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		String speed = "5.344565";
		Integer heartRate = 120;

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(zonedDateTime, index, position, altitude, distance,
				speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(speed));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointFromZonedDateTimeBasicDoubleDataTest() {
		// Given
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		Double altitude = 345.34;
		Double distance = 23.4566;
		Double speed = 5.344565;
		Integer heartRate = 120;

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(zonedDateTime, index, position, altitude, distance,
				speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(String.valueOf(altitude)));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(String.valueOf(distance)));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(String.valueOf(speed)));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointXmlGregorianCalendarDataTest() {
		// Given
		XMLGregorianCalendar xmlGregorianCalendar = Try.of(() -> DatatypeFactory.newInstance())
				.onFailure(err -> log.error("It couldn't be created the xml data type factory", err))
				.map(datatypeFactory -> datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()))
				.getOrNull();
		xmlGregorianCalendar.setDay(25);
		xmlGregorianCalendar.setMonth(2);
		xmlGregorianCalendar.setYear(2020);
		xmlGregorianCalendar.setTime(15, 56, 0);
		xmlGregorianCalendar.setTimezone(60);
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		String speed = "5.344565";
		Integer heartRate = 120;

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(xmlGregorianCalendar, index, position, altitude, distance,
				speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(speed));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointTimeMillisDataTest() {
		// Given
		long timiInMilli = 1582642560000L;
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		String speed = "5.344565";
		Integer heartRate = 120;

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(timiInMilli, index, position, altitude, distance,
				speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(speed));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointFromZonedDateMoreDataCoordinatesTest() {
		// Given
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		String speed = "5.344565";
		Integer heartRate = 120;
		doReturn(position).when(positionOperations).toPosition(latitude, longitude);

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(zonedDateTime, index, latitude, longitude, altitude,
				distance, speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(speed));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointTimeMillisDataCoordinatesTest() {
		// Given
		long timiInMilli = 1582642560000L;
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.6132120";
		String longitude = "-6.5734430";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		String speed = "5.344565";
		Integer heartRate = 120;
		doReturn(position).when(positionOperations).toPosition(latitude, longitude);

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(timiInMilli, index, latitude, longitude, altitude,
				distance, speed, heartRate);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getSpeed()).isEqualTo(new BigDecimal(speed));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
	}

	@Test
	public void toTrackPointMapperFromTrackPointTAndIndex() {
		// Given
		XMLGregorianCalendar xmlGregorianCalendar = Try.of(() -> DatatypeFactory.newInstance())
				.onFailure(err -> log.error("It couldn't be created the xml data type factory", err))
				.map(datatypeFactory -> datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()))
				.getOrNull();
		xmlGregorianCalendar.setDay(25);
		xmlGregorianCalendar.setMonth(2);
		xmlGregorianCalendar.setYear(2020);
		xmlGregorianCalendar.setTime(15, 56, 0);
		xmlGregorianCalendar.setTimezone(60);
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.613212";
		String longitude = "-6.573443";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";
		String distance = "23.4566";
		Integer heartRate = 120;

		PositionT positionT = new PositionT();
		positionT.setLatitudeDegrees(Double.valueOf(latitude));
		positionT.setLongitudeDegrees(Double.valueOf(longitude));

		TrackpointT trackpointT = new TrackpointT();
		trackpointT.setTime(xmlGregorianCalendar);
		trackpointT.setPosition(positionT);
		trackpointT.setDistanceMeters(Double.valueOf(distance));
		trackpointT.setAltitudeMeters(Double.valueOf(altitude));
		HeartRateInBeatsPerMinuteT heartRateT = new HeartRateInBeatsPerMinuteT();
		heartRateT.setValue(heartRate.shortValue());
		trackpointT.setHeartRateBpm(heartRateT);

		doReturn(of(position)).when(positionOperations).toPosition(trackpointT);

		// When
		TrackPoint trackPoint = trackPointOperations.toTrackPoint(trackpointT, index);

		// Then
		assertThat(trackPoint).isNotNull();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		assertThat(trackPoint.getDistanceMeters()).isEqualTo(new BigDecimal(distance));
		assertThat(trackPoint.getHeartRateBpm()).isEqualTo(heartRate);
		verify(positionOperations).toPosition(eq(trackpointT));
	}

	@Test
	public void toTrackPointMapperFromWptTypeAndIndex() {
		// Given
		XMLGregorianCalendar xmlGregorianCalendar = Try.of(() -> DatatypeFactory.newInstance())
				.onFailure(err -> log.error("It couldn't be created the xml data type factory", err))
				.map(datatypeFactory -> datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()))
				.getOrNull();
		xmlGregorianCalendar.setDay(25);
		xmlGregorianCalendar.setMonth(2);
		xmlGregorianCalendar.setYear(2020);
		xmlGregorianCalendar.setTime(15, 56, 0);
		xmlGregorianCalendar.setTimezone(60);
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 25, 14,56);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
		int index = 2;
		String latitude = "42.613212";
		String longitude = "-6.573443";
		Position position = Position.builder()
				.latitudeDegrees(new BigDecimal(latitude))
				.longitudeDegrees(new BigDecimal(longitude))
				.build();
		String altitude = "345.34";

		WptType wptType = new WptType();
		wptType.setLat(new BigDecimal(latitude));
		wptType.setLon(new BigDecimal(longitude));
		wptType.setEle(new BigDecimal(altitude));
		wptType.setTime(xmlGregorianCalendar);

		doReturn(of(position)).when(positionOperations).toPosition(wptType);

		// When
		Optional<TrackPoint> optTrackPoint = trackPointOperations.toTrackPoint(wptType, index);

		// Then
		assertThat(optTrackPoint).isNotEmpty();
		TrackPoint trackPoint = optTrackPoint.get();
		assertThat(trackPoint.getDate()).isEqualTo(zonedDateTime);
		assertThat(trackPoint.getPosition()).isEqualTo(position);
		assertThat(trackPoint.getIndex()).isEqualTo(index);
		assertThat(trackPoint.getAltitudeMeters()).isEqualTo(new BigDecimal(altitude));
		verify(positionOperations).toPosition(eq(wptType));
	}

}
