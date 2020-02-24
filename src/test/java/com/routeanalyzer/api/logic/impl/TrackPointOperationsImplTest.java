package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;

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
	
	@Before
	public void setUp() {
		// Given
		oviedo = toPosition("43.3602900", "-5.8447600");
		madrid = toPosition("40.4165000", "-3.7025600");
		park = toPosition("43.352478", "-5.8501170");
		LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 12, 0, 5, 0);
		ZoneId zoneId = ZoneId.systemDefault();
		Clock fixed = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), zoneId);
		ZonedDateTime now = ZonedDateTime.now(fixed);
		oviedoTrack = toTrackPoint(now, 1, oviedo, "100", "0",
				null, 78);
		parkTrack = toTrackPoint(now.plusMinutes(15L), 2,
				park, "100", "970.64", null, 78);
		madridTrack = toTrackPoint(now.plusHours(6L), 3,
				madrid, "100", "372247.30", null, 78);
		latParam = toBigDecimal("12");
		lngParam = toBigDecimal("6");
	}

	@Test
	public void isThisTrackPositionEqualTest() {
		// Given
		Position position = toPosition("12", "6");
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, position, "450", "0", "0", 150);
		doReturn(true).when(positionOperations).isThisPosition(eq(position),  eq(latParam), eq(lngParam));
		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionNullTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, null, "450", "0", "0", 150);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionLatNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("5.67", "6"), 12123123L, 0);

		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionLngNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);

		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "3"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionIndexEqualTimeMillisNotEqualTest() {
		// Given
		Position position = toPosition("12", "6");
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, position, "450", "0", "0", 150);

		doReturn(true).when(positionOperations).isThisPosition(eq(position), eq(latParam), eq(lngParam));
		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "6"), 12L, 0);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionTimeMillisEqualIndexNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		Position position = trackpoint.getPosition();
		doReturn(true).when(positionOperations).isThisPosition(eq(position), eq(latParam), eq(lngParam));
		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 13);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionIndexTimeMillisNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 3, "12", "6", "450", "0", "0", 150);
		Position position = trackpoint.getPosition();
		doReturn(true).when(positionOperations).isThisPosition(eq(position), eq(latParam), eq(lngParam));
		// When
		boolean isThisTrack = trackPointOperations.isThisTrack(trackpoint, toPosition("12", "6"), 12L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void calculateSpeed() {
		// When
		doReturn(18000.0).when(positionOperations).calculateDistance(eq(oviedo), eq(park));
		Double speedOvdPark = trackPointOperations.calculateSpeed(oviedoTrack, parkTrack);
		doReturn(931500.0).when(positionOperations).calculateDistance(eq(park), eq(madrid));
		Double speedParkMad = trackPointOperations.calculateSpeed(parkTrack, madridTrack);
		// Then
		assertThat(speedOvdPark).isEqualTo(20);
		assertThat(speedParkMad).isEqualTo(45);
	}

	@Test
	public void calculateSpeedSamePoint() {
		// Given
		TrackPoint oviedoTrack = toTrackPoint(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), 1,
				oviedo, "100", "10", null, 78);
		// When 
		Double speedOvdOvd = trackPointOperations.calculateSpeed(oviedoTrack, oviedoTrack);
		// Then
		assertThat(speedOvdOvd).isNull();
	}

}
