package com.routeanalyzer.test.logic;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.routeanalyzer.common.CommonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.routeanalyzer.logic.TrackPointUtils;
import com.routeanalyzer.logic.impl.TrackPointUtilsImpl;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import static com.routeanalyzer.common.CommonUtils.toPosition;
import static com.routeanalyzer.common.CommonUtils.round;
import static com.routeanalyzer.common.CommonUtils.toTrackPoint;

@RunWith(SpringJUnit4ClassRunner.class)
public class TrackPointUtilsTest {

	private TrackPointUtils trackpointUtils = new TrackPointUtilsImpl();
	private Position oviedo;
	private Position madrid;
	private Position park;
	private TrackPoint oviedoTrack;
	private TrackPoint parkTrack;
	private TrackPoint madridTrack;
	
	@Before
	public void setUp() {
		// Given
		oviedo = toPosition("43.3602900", "-5.8447600");
		madrid = toPosition("40.4165000", "-3.7025600");
		park = toPosition("43.352478", "-5.8501170");
		LocalDateTime now = LocalDateTime.now();
		oviedoTrack = toTrackPoint(now, 1, oviedo, "100", "0",
				null, 78);
		parkTrack = toTrackPoint(now.plusMinutes(15L), 2,
				park, "100", "970.64", null, 78);
		madridTrack = toTrackPoint(now.plusHours(6L), 3,
				madrid, "100", "372247.30", null, 78);
	}

	@Test
	public void isThisTrackPositionNullTest() {
		// Given
		TrackPoint trackpoint = CommonUtils.toTrackPoint(12123123L, 0, null, "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionLatNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("5.67", "6"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionLngNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "3"), 12123123L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void isThisTrackPositionIndexEqualTimeMillisNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "6"), 12L, 0);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionTimeMillisEqualIndexNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "6"), 12123123L, 13);
		// Then
		assertThat(isThisTrack).isTrue();
	}

	@Test
	public void isThisTrackPositionIndexTimeMillisNotEqualTest() {
		// Given
		TrackPoint trackpoint = toTrackPoint(12123123L, 3, "12", "6", "450", "0", "0", 150);
		// When
		boolean isThisTrack = trackpointUtils.isThisTrack(trackpoint, toPosition("12", "6"), 12L, 0);
		// Then
		assertThat(isThisTrack).isFalse();
	}

	@Test
	public void calculateDistanceTest() {
		// When
		double distanceOvdPark = trackpointUtils.calculateDistance(oviedo, park);
		double distanceOvdMad = trackpointUtils.calculateDistance(oviedo, madrid);
		// Then
		assertThat(round(distanceOvdPark, 2)).isEqualTo(970.64);
		assertThat(round(distanceOvdMad, 2)).isEqualTo(372247.30);
	}

	@Test
	public void calculateDistanceSamePoint() {
		// When
		double distanceOvdOvd = trackpointUtils.calculateDistance(oviedo, oviedo);
		// Then
		assertThat(round(distanceOvdOvd, 2)).isEqualTo(0.0);
	}

	@Test
	public void calculateSpeed() {
		// When
		double speedOvdPark = trackpointUtils.calculateSpeed(oviedoTrack, parkTrack);
		double speedParkMad = trackpointUtils.calculateSpeed(parkTrack, madridTrack);
		// Then
		assertThat(round(speedOvdPark, 2)).isEqualTo(1.08);
		assertThat(round(speedParkMad, 2)).isEqualTo(17.96);
	}

	@Test
	public void calculateSpeedSamePoint() {
		// Given
		TrackPoint oviedoTrack = CommonUtils.toTrackPoint(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), 1,
				oviedo, "100", "10", null, 78);
		// When 
		double speedOvdOvd = trackpointUtils.calculateSpeed(oviedoTrack, oviedoTrack);
		// Then
		assertThat(round(speedOvdOvd, 2)).isEqualTo(0.0);
	}

}
