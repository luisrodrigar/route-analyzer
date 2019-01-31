package com.routeanalyzer.test.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.routeanalyzer.logic.TrackPointUtils;
import com.routeanalyzer.logic.impl.TrackPointUtilsImpl;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

@RunWith(SpringJUnit4ClassRunner.class)
public class TrackPointUtilsTest {

	TrackPointUtils trackpointUtils = new TrackPointUtilsImpl();

	private TrackPoint getTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
			String speed, int heartRate) {
		return new TrackPoint(new Date(timeMillis), new Integer(index),
				lat!=null && lng!=null ? new Position(new BigDecimal(lat), new BigDecimal(lng)):null, new BigDecimal(alt), new BigDecimal(dist),
				new BigDecimal(speed), heartRate);
	}

	private TrackPoint getTrackPointPosition(long timeMillis, int index, Position position, String alt, String dist,
			String speed, int heartRate) {
		return getTrackPoint(timeMillis, index, position!=null?String.valueOf(position.getLatitudeDegrees().doubleValue()):null,
				position!=null?String.valueOf(position.getLongitudeDegrees().doubleValue()):null, alt, dist, speed, heartRate);
	}

	@Test
	public void isThisTrackPositionNullTest() {
		TrackPoint trackpoint = getTrackPointPosition(12123123L, 0, null, "450", "0", "0", 150);
		assertFalse(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("6")),
				12123123L, new Integer(0)));
	}

	@Test
	public void isThisTrackPositionEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		assertTrue(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("6")),
				12123123L, new Integer(0)));
	}

	@Test
	public void isThisTrackPositionLatNotEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		assertFalse(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("5.67"), new BigDecimal("6")),
				12123123L, new Integer(0)));
	}

	@Test
	public void isThisTrackPositionLngNotEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		assertFalse(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("3")),
				12123123L, new Integer(0)));
	}
	
	@Test
	public void isThisTrackPositionIndexEqualTimeMillisNotEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		assertTrue(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("6")),
				12L, new Integer(0)));
	}
	
	@Test
	public void isThisTrackPositionTimeMillisEqualIndexNotEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 0, "12", "6", "450", "0", "0", 150);
		assertTrue(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("6")),
				12123123L, new Integer(13)));
	}
	
	@Test
	public void isThisTrackPositionIndexTimeMillisNotEqualTest() {
		TrackPoint trackpoint = getTrackPoint(12123123L, 3, "12", "6", "450", "0", "0", 150);
		assertFalse(trackpointUtils.isThisTrack(trackpoint, new Position(new BigDecimal("12"), new BigDecimal("6")),
				12L, new Integer(0)));
	}

}
