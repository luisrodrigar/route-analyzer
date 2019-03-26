package com.routeanalyzer.api.logic.impl;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.googlemaps.GoogleMapsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.DateUtils.toLocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class LapsOperationsImpTest {

    @Mock
    private TrackPointOperations trackPointOperations;
    @Mock
    private GoogleMapsServiceImpl googleMapsService;
    @InjectMocks
    private LapsOperationsImpl lapsOperations;

    private Lap lapLeft;
    private Lap lapRight;
    private List<TrackPoint> trackPointsLeft;
    private List<TrackPoint> trackPointsRight;
    private TrackPoint trackPointLeft1, trackPointLeft2, trackPointLeft3, trackPointLeft4;
    private long timeMillisLeft1, timeMillisLeft2, timeMillisLeft3, timeMillisLeft4;
    private TrackPoint trackPointRight1, trackPointRight2, trackPointRight3, trackPointRight4;
    private long timeMillisRight1, timeMillisRight2, timeMillisRight3, timeMillisRight4;
    private Position oviedo;
    private Position park;

    @Before
    public void setUp() {
        oviedo = toPosition("43.3602900", "-5.8447600");
        park = toPosition("43.352478", "-5.8501170");
        addLeftTracks();
        addRightTracks();
    }

    private void addLeftTracks() {
        createLeftTrack();
        trackPointsLeft = Lists.newArrayList();
        trackPointsLeft.add(trackPointLeft1);
        trackPointsLeft.add(trackPointLeft2);
        trackPointsLeft.add(trackPointLeft3);
        trackPointsLeft.add(trackPointLeft4);
    }

    private void addRightTracks() {
        createRightTrack();
        trackPointsRight = Lists.newArrayList();
        trackPointsRight.add(trackPointRight1);
        trackPointsRight.add(trackPointRight2);
        trackPointsRight.add(trackPointRight3);
        trackPointsRight.add(trackPointRight4);
    }

    private void createLeftTrack() {
        timeMillisLeft1 = 123456L;
        timeMillisLeft2 = 123466L;
        timeMillisLeft3 = 123476L;
        timeMillisLeft4 = 123486L;

        trackPointLeft1 = toTrackPoint(timeMillisLeft1, 3, "43.3602900", "-5.8447600", "120"
                , "25.0", "12.0", new Integer(76));
        trackPointLeft2 = toTrackPoint(timeMillisLeft2, 4, "43.352478", "-5.8501170", "120"
                , "25.0", "12.0", new Integer(86));
        trackPointLeft3 = toTrackPoint(timeMillisLeft3, 5, "44.3602900", "-6.8447600", "120"
                , "25.0", "35.0", new Integer(90));
        trackPointLeft4 = toTrackPoint(timeMillisLeft4, 6, "46.352478", "-4.8501170", "120"
                , "25.0", "12.0", new Integer(95));
    }

    private void createRightTrack() {
        timeMillisRight1 = 123506L;
        timeMillisRight2 = 123526L;
        timeMillisRight3 = 123546L;
        timeMillisRight4 = 123606L;

        trackPointRight1 = toTrackPoint(timeMillisRight1, 7, "42.3602900", "-3.8447600", "120"
                , "50.0", "12.0", new Integer(100));
        trackPointRight2 = toTrackPoint(timeMillisRight2, 8, "46.452478", "-6.9501170", "120"
                , "50.0", "12.0", new Integer(107));
        trackPointRight3 = toTrackPoint(timeMillisRight3, 9, "40.3602900", "-8.8447600", "120"
                , "25.0", "12.0", new Integer(112));
        trackPointRight4 = toTrackPoint(timeMillisRight4, 10, "40.352478", "-9.8501170", "120"
                , "25.0", "22.0", new Integer(123));
    }

    @Test
    public void joinLapsTest() {
        // Given
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(2)
                .totalTimeSeconds(50.0)
                .intensity("LOW")
                .build();
        lapRight = Lap.builder().tracks(trackPointsRight)
                .distanceMeters(150.0)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(3)
                .totalTimeSeconds(50.0)
                .intensity("HIGH")
                .build();
        // When
        Lap result = lapsOperations.joinLaps(lapLeft, lapRight);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTracks().size()).isEqualTo(8);
        assertThat(result.getTracks().get(4)).isEqualTo(trackPointRight1);
        assertThat(result.getIntensity()).isEqualTo("HIGH");
        assertThat(result.getDistanceMeters()).isEqualTo(250);
        assertThat(result.getIndex()).isEqualTo(2);
        assertThat(result.getMaximumHeartRate()).isEqualTo(123);
        assertThat(result.getMaximumSpeed()).isEqualTo(35);
        assertThat(result.getAverageHearRate()).isEqualTo(98.625);
        assertThat(result.getAverageSpeed()).isEqualTo(16.125);
    }

    @Test
    public void joinLapWithNoHeartRateValuesTest() {
        // Given
        Consumer<TrackPoint> resetHeartRateValues = trackPoint -> trackPoint.setHeartRateBpm(null);
        Consumer<TrackPoint> resetSpeedValues = trackPoint -> trackPoint.setSpeed(null);
        trackPointsLeft.stream().forEach(resetHeartRateValues);
        trackPointsRight.stream().forEach(resetHeartRateValues);
        trackPointsLeft.stream().forEach(resetSpeedValues);
        trackPointsRight.stream().forEach(resetSpeedValues);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(0)
                .totalTimeSeconds(50.0)
                .intensity("HIGH")
                .build();
        lapRight = Lap.builder().tracks(trackPointsRight)
                .distanceMeters(150.0)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(1)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        Lap result = lapsOperations.joinLaps(lapLeft, lapRight);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTracks().size()).isEqualTo(8);
        assertThat(result.getTracks().get(3)).isEqualTo(trackPointLeft4);
        assertThat(result.getIntensity()).isEqualTo("HIGH");
        assertThat(result.getDistanceMeters()).isEqualTo(250);
        assertThat(result.getIndex()).isEqualTo(0);
        assertThat(result.getMaximumHeartRate()).isNull();
        assertThat(result.getMaximumSpeed()).isNull();
        assertThat(result.getAverageHearRate()).isNull();
        assertThat(result.getAverageSpeed()).isNull();
    }

    @Test
    public void joinLapWithLapAggregateMaxValueTest() {
        // Given
        trackPointsLeft.remove(trackPointLeft1);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(0)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        lapRight = Lap.builder().tracks(trackPointsRight)
                .distanceMeters(150.0)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(1)
                .maximumHeartRate(156)
                .maximumSpeed(22.0)
                .averageHearRate(100.03)
                .averageSpeed(11.00)
                .totalTimeSeconds(50.0)
                .intensity("MEDIUM")
                .build();
        // When
        Lap result = lapsOperations.joinLaps(lapLeft, lapRight);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTracks().size()).isEqualTo(7);
        assertThat(result.getTracks().get(0)).isEqualTo(trackPointLeft2);
        assertThat(result.getIntensity()).isEqualTo("MEDIUM");
        assertThat(result.getDistanceMeters()).isEqualTo(250);
        assertThat(result.getIndex()).isEqualTo(0);
        assertThat(result.getMaximumHeartRate()).isEqualTo(123);
        assertThat(result.getMaximumSpeed()).isEqualTo(35);
        assertThat(MathUtils.round(result.getAverageHearRate(), 2)).isEqualTo(101.86);
        assertThat(MathUtils.round(result.getAverageSpeed(), 2)).isEqualTo(16.71);
    }

    @Test
    public void joinLapWithIntensityNullTest() {
        // Given
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        lapRight = Lap.builder().tracks(trackPointsRight)
                .distanceMeters(150.0)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(9)
                .maximumHeartRate(156)
                .maximumSpeed(22.0)
                .averageHearRate(100.03)
                .averageSpeed(11.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        Lap result = lapsOperations.joinLaps(lapLeft, lapRight);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIntensity()).isNull();
        assertThat(result.getIndex()).isEqualTo(8);
    }


    @Test
    public void calculateAltitudeTest() {
        // Given
        Consumer<TrackPoint> resetAltValues = trackPoint -> trackPoint.setAltitudeMeters(null);
        trackPointsLeft.stream().forEach(resetAltValues);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        BigDecimal ele1 = toBigDecimal(245.0);
        BigDecimal ele2 = toBigDecimal(285.0);
        BigDecimal ele3 = toBigDecimal(300.0);
        BigDecimal ele4 = toBigDecimal(225.0);
        // When
        String positions = trackPointLeft1.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft1.getPosition().getLongitudeDegrees() + "|"
                + trackPointLeft2.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft2.getPosition().getLongitudeDegrees() + "|"
                + trackPointLeft3.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft3.getPosition().getLongitudeDegrees() + "|"
                + trackPointLeft4.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft4.getPosition().getLongitudeDegrees();
        doReturn(positions).when(googleMapsService).createPositionsRequest(anyList());
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "OK");
        String key1 = trackPointLeft1.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft1.getPosition().getLongitudeDegrees();
        result.put(key1, ele1.toString());
        String key2 = trackPointLeft2.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft2.getPosition().getLongitudeDegrees();
        result.put(key2, ele2.toString());
        String key3 = trackPointLeft3.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft3.getPosition().getLongitudeDegrees();
        result.put(key3, ele3.toString());
        String key4 = trackPointLeft4.getPosition().getLatitudeDegrees() + ","
                + trackPointLeft4.getPosition().getLongitudeDegrees();
        result.put(key4, ele4.toString());
        doReturn(result).when(googleMapsService).getAltitude(eq(positions));
        doReturn(key1).when(googleMapsService).getCoordinatesCode(eq(trackPointLeft1));
        doReturn(key2).when(googleMapsService).getCoordinatesCode(eq(trackPointLeft2));
        doReturn(key3).when(googleMapsService).getCoordinatesCode(eq(trackPointLeft3));
        doReturn(key4).when(googleMapsService).getCoordinatesCode(eq(trackPointLeft4));
        lapsOperations.calculateAltitude(lapLeft);
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getAltitudeMeters()).isEqualTo(ele1);
        assertThat(lapLeft.getTracks().get(1).getAltitudeMeters()).isEqualTo(ele2);
        assertThat(lapLeft.getTracks().get(2).getAltitudeMeters()).isEqualTo(ele3);
        assertThat(lapLeft.getTracks().get(3).getAltitudeMeters()).isEqualTo(ele4);
        verify(googleMapsService).createPositionsRequest(anyList());
        verify(googleMapsService).getAltitude(positions);
        verify(googleMapsService, times(4)).getCoordinatesCode(any());
    }

    @Test
    public void calculateAltitudeFailAltitudeServiceTest() {
        // Given
        Consumer<TrackPoint> resetAltValues = trackPoint -> trackPoint.setAltitudeMeters(null);
        trackPointsLeft.stream().forEach(resetAltValues);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        String positions = "fail_service_request" ;
        doReturn(positions).when(googleMapsService).createPositionsRequest(anyList());
        Map<String, String> result = Maps.newHashMap();
        result.put("status", "BAD_REQUEST");
        doReturn(result).when(googleMapsService).getAltitude(eq(positions));
        lapsOperations.calculateAltitude(lapLeft);
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(1).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(2).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(3).getAltitudeMeters()).isNull();
    }

    @Test
    public void calculateAltitudeTrackPointsHasAltitudeTest() {
        // Given

        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        lapsOperations.calculateAltitude(lapLeft);
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getAltitudeMeters()).isEqualTo(toBigDecimal(120.0));
        assertThat(lapLeft.getTracks().get(1).getAltitudeMeters()).isEqualTo(toBigDecimal(120.0));
        assertThat(lapLeft.getTracks().get(2).getAltitudeMeters()).isEqualTo(toBigDecimal(120.0));
        assertThat(lapLeft.getTracks().get(3).getAltitudeMeters()).isEqualTo(toBigDecimal(120.0));
    }

    @Test
    public void calculateAltitudeTrackPointsHasNoPositionTest() {
        // Given
        Consumer<TrackPoint> resetAltValues = trackPoint -> trackPoint.setAltitudeMeters(null);
        trackPointsLeft.stream().forEach(resetAltValues);
        Consumer<TrackPoint> resetPosition = trackPoint -> trackPoint.setPosition(null);
        trackPointsLeft.stream().forEach(resetPosition);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        lapsOperations.calculateAltitude(lapLeft);
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(1).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(2).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(3).getAltitudeMeters()).isNull();
    }

    @Test
    public void calculateAltitudeTrackPointsHasPositionEmptyValuesTest() {
        // Given
        Consumer<TrackPoint> resetAltValues = trackPoint -> trackPoint.setAltitudeMeters(null);
        trackPointsLeft.stream().forEach(resetAltValues);
        Consumer<TrackPoint> resetPosition = trackPoint -> trackPoint.setPosition(Position.builder().build());
        trackPointsLeft.stream().forEach(resetPosition);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .distanceMeters(100.0)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(8)
                .maximumHeartRate(176)
                .maximumSpeed(22.0)
                .averageHearRate(123.03)
                .averageSpeed(14.00)
                .totalTimeSeconds(50.0)
                .intensity(null)
                .build();
        // When
        lapsOperations.calculateAltitude(lapLeft);
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(1).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(2).getAltitudeMeters()).isNull();
        assertThat(lapLeft.getTracks().get(3).getAltitudeMeters()).isNull();
    }

    @Test
    public void calculateDistanceLapInTheMiddle() {
        // Given
        Consumer<TrackPoint> resetDistValues = trackPoint -> trackPoint.setDistanceMeters(null);
        trackPointsRight.stream().forEach(resetDistValues);
        lapRight = Lap.builder().tracks(trackPointsRight)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(3)
                .totalTimeSeconds(50.0)
                .intensity("HIGH")
                .build();
        double dist1 = 10.0;
        double dist2 = 8.0;
        double dist3 = 12.0;
        double dist4 = 5.0;
        double previousDistance = trackPointLeft4.getDistanceMeters().doubleValue();
        // When
        doReturn(dist1).when(trackPointOperations).calculateDistance(eq(trackPointLeft4), eq(trackPointRight1));
        doReturn(dist2).when(trackPointOperations).calculateDistance(eq(trackPointRight1), eq(trackPointRight2));
        doReturn(dist3).when(trackPointOperations).calculateDistance(eq(trackPointRight2), eq(trackPointRight3));
        doReturn(dist4).when(trackPointOperations).calculateDistance(eq(trackPointRight3), eq(trackPointRight4));
        lapsOperations.calculateDistanceLap(lapRight, trackPointLeft4);
        // Then
        assertThat(lapRight).isNotNull();
        assertThat(lapRight.getTracks().get(0).getDistanceMeters()).isEqualTo(
                toBigDecimal(previousDistance + dist1));
        assertThat(lapRight.getTracks().get(1).getDistanceMeters()).isEqualTo(
                toBigDecimal(previousDistance + dist1 + dist2));
        assertThat(lapRight.getTracks().get(2).getDistanceMeters()).isEqualTo(
                toBigDecimal(previousDistance + dist1 + dist2 + dist3));
        assertThat(lapRight.getTracks().get(3).getDistanceMeters()).isEqualTo(
                toBigDecimal(previousDistance + dist1 + dist2 + dist3 + dist4));
        verify(trackPointOperations, times(1)).calculateDistance(trackPointLeft4, trackPointRight1);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointRight1, trackPointRight2);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointRight2, trackPointRight3);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointRight3, trackPointRight4);
    }

    @Test
    public void calculateDistanceLapLocatedFirstTest() {
        // Given
        Consumer<TrackPoint> resetDistValues = trackPoint -> trackPoint.setDistanceMeters(null);
        trackPointsLeft.stream().forEach(resetDistValues);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(0)
                .build();
        double dist1 = 0.0;
        double dist2 = 9.0;
        double dist3 = 11.0;
        double dist4 = 45.0;
        // When
        doReturn(dist2).when(trackPointOperations).calculateDistance(eq(trackPointLeft1), eq(trackPointLeft2));
        doReturn(dist3).when(trackPointOperations).calculateDistance(eq(trackPointLeft2), eq(trackPointLeft3));
        doReturn(dist4).when(trackPointOperations).calculateDistance(eq(trackPointLeft3), eq(trackPointLeft4));
        lapsOperations.calculateDistanceLap(lapLeft, null);
        // Then
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getDistanceMeters()).isEqualTo(
                toBigDecimal(dist1));
        assertThat(lapLeft.getTracks().get(1).getDistanceMeters()).isEqualTo(
                toBigDecimal(dist1 + dist2));
        assertThat(lapLeft.getTracks().get(2).getDistanceMeters()).isEqualTo(
                toBigDecimal(dist1 + dist2 + dist3));
        assertThat(lapLeft.getTracks().get(3).getDistanceMeters()).isEqualTo(
                toBigDecimal(dist1 + dist2 + dist3 + dist4));
        verify(trackPointOperations, times(0)).calculateDistance(null, trackPointLeft1);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointLeft1, trackPointLeft2);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointLeft2, trackPointLeft3);
        verify(trackPointOperations, times(1)).calculateDistance(trackPointLeft3, trackPointLeft4);
    }

    private Consumer<TrackPoint> resetSpeedValue = trackPoint -> trackPoint.setSpeed(null);

    @Test
    public void calculateSpeedLapInTheMiddleTest() {
        // Given
        trackPointsRight.stream().forEach(resetSpeedValue);
        lapRight = Lap.builder().tracks(trackPointsRight)
                .startTime(toLocalDateTime(timeMillisRight1).orElse(null))
                .index(0)
                .build();
        double speed1 = 5.0;
        double speed2 = 10.0;
        double speed3 = 15.0;
        double speed4 = 23.0;
        // When
        doReturn(speed1).when(trackPointOperations).calculateSpeed(eq(trackPointLeft4), eq(trackPointRight1));
        doReturn(speed2).when(trackPointOperations).calculateSpeed(eq(trackPointRight1), eq(trackPointRight2));
        doReturn(speed3).when(trackPointOperations).calculateSpeed(eq(trackPointRight2), eq(trackPointRight3));
        doReturn(speed4).when(trackPointOperations).calculateSpeed(eq(trackPointRight3), eq(trackPointRight4));
        lapsOperations.calculateSpeedLap(lapRight, trackPointLeft4);
        // Then
        assertThat(lapRight).isNotNull();
        assertThat(lapRight.getTracks().get(0).getSpeed()).isEqualTo(
                toBigDecimal(speed1));
        assertThat(lapRight.getTracks().get(1).getSpeed()).isEqualTo(
                toBigDecimal(speed2));
        assertThat(lapRight.getTracks().get(2).getSpeed()).isEqualTo(
                toBigDecimal(speed3));
        assertThat(lapRight.getTracks().get(3).getSpeed()).isEqualTo(
                toBigDecimal(speed4));
        assertThat(lapRight.getTotalTimeSeconds())
                .isEqualTo(DateUtils.millisToSeconds(Double.valueOf(timeMillisRight4)
                        - Double.valueOf(timeMillisRight1)));
        assertThat(lapRight.getDistanceMeters())
                .isEqualTo(
                        trackPointRight4.getDistanceMeters().subtract(trackPointRight1.getDistanceMeters()).doubleValue());
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointLeft4, trackPointRight1);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointRight1, trackPointRight2);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointRight2, trackPointRight3);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointRight3, trackPointRight4);
    }

    @Test
    public void calculateSpeedLapLocatedFirstTest() {
        // Given
        trackPointsLeft.stream().forEach(resetSpeedValue);
        lapLeft = Lap.builder().tracks(trackPointsLeft)
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(0)
                .build();
        double speed1 = 0.0;
        double speed2 = 9.0;
        double speed3 = 11.0;
        double speed4 = 45.0;
        // When
        doReturn(speed2).when(trackPointOperations).calculateSpeed(eq(trackPointLeft1), eq(trackPointLeft2));
        doReturn(speed3).when(trackPointOperations).calculateSpeed(eq(trackPointLeft2), eq(trackPointLeft3));
        doReturn(speed4).when(trackPointOperations).calculateSpeed(eq(trackPointLeft3), eq(trackPointLeft4));
        lapsOperations.calculateSpeedLap(lapLeft, null);
        // Then
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().get(0).getSpeed()).isEqualTo(
                toBigDecimal(speed1));
        assertThat(lapLeft.getTracks().get(1).getSpeed()).isEqualTo(
                toBigDecimal(speed2));
        assertThat(lapLeft.getTracks().get(2).getSpeed()).isEqualTo(
                toBigDecimal(speed3));
        assertThat(lapLeft.getTracks().get(3).getSpeed()).isEqualTo(
                toBigDecimal(speed4));
        assertThat(lapLeft.getAverageSpeed()).isEqualTo(16.25);
        assertThat(lapLeft.getMaximumSpeed()).isEqualTo(45.00);
        assertThat(lapLeft.getTotalTimeSeconds())
                .isEqualTo(DateUtils.millisToSeconds(Double.valueOf(timeMillisLeft4)
                        - Double.valueOf(timeMillisLeft1)));
        assertThat(lapLeft.getDistanceMeters())
                .isEqualTo(
                        trackPointLeft4.getDistanceMeters().subtract(trackPointLeft1.getDistanceMeters()).doubleValue());
        verify(trackPointOperations, times(0)).calculateSpeed(null, trackPointLeft2);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointLeft1, trackPointLeft2);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointLeft2, trackPointLeft3);
        verify(trackPointOperations, times(1)).calculateSpeed(trackPointLeft3, trackPointLeft4);
    }

    @Test
    public void calculateSpeedLapEmptyTracks() {
        // Given
        trackPointsLeft.stream().forEach(resetSpeedValue);
        lapLeft = Lap.builder()
                .startTime(toLocalDateTime(timeMillisLeft1).orElse(null))
                .index(0)
                .build();
        // When
        lapsOperations.calculateSpeedLap(lapLeft, null);
        // Then
        assertThat(lapLeft).isNotNull();
        assertThat(lapLeft.getTracks().isEmpty()).isTrue();
        assertThat(lapLeft.getAverageSpeed()).isNull();
        assertThat(lapLeft.getMaximumSpeed()).isNull();
        assertThat(lapLeft.getTotalTimeSeconds()).isNull();
        assertThat(lapLeft.getDistanceMeters()).isNull();
        verify(trackPointOperations, times(0)).calculateSpeed(any(), any());
    }

}
