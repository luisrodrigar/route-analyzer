package com.routeanalyzer.api.services.googlemaps;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static java.util.stream.Collectors.toMap;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GoogleMapsServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleMapsServiceImpl elevationService;

    @Test
    public void getAltitudeTest() {
        // Given
        double latitude1 = 7.3212;
        double longitude1 = 1.3419;
        double latitude2 = 4.3212;
        double longitude2 = 2.3419;
        double latitude3 = 3.6012;
        double longitude3 = 0.3419;
        String elevation1 = "450.31";
        String elevation2 = "340.31";
        String elevation3 = "560.31";
        GMPosition gmPosition1 = GMPosition.builder()
                .lat(latitude1)
                .lng(longitude1)
                .build();
        GMPosition gmPosition2 = GMPosition.builder()
                .lat(latitude2)
                .lng(longitude2)
                .build();
        GMPosition gmPosition3 = GMPosition.builder()
                .lat(latitude3)
                .lng(longitude3)
                .build();
        GMResult gmResult1 = GMResult.builder()
                .location(gmPosition1)
                .elevation(Double.parseDouble(elevation1))
                .build();
        GMResult gmResult2 = GMResult.builder()
                .location(gmPosition2)
                .elevation(Double.parseDouble(elevation2))
                .build();
        GMResult gmResult3 = GMResult.builder()
                .location(gmPosition3)
                .elevation(Double.parseDouble(elevation3))
                .build();
        GMResponse gmResponse = GMResponse.builder()
                .results(of(gmResult1, gmResult2, gmResult3).collect(Collectors.toList()))
                .status("OK")
                .build();
        Map<String, String> expectedResult = of(new String[][] {
                { (latitude1 + "," + longitude1), elevation1 },
                { (latitude2 + "," + longitude2), elevation2 },
                { (latitude3 + "," + longitude3), elevation3 },
                { "status", "OK"}
        }).collect(toMap(data -> data[0], data -> data[1]));

        // When
        doReturn(gmResponse).when(restTemplate).getForObject(anyString(), eq(GMResponse.class));
        Map<String, String> mapResults = elevationService.getAltitude("anyPosition");

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isNotEmpty();
        assertThat(mapResults).isEqualTo(expectedResult);
    }

    @Test
    public void getAltitudeFailServiceTest() {
        // Given
        GMResponse gmResponse = GMResponse.builder()
                .results(Collections.emptyList())
                .status("KO")
                .build();
        Map<String, String> expectedResult = of(new String[][] {
                { "status", "KO"}
        }).collect(toMap(data -> data[0], data -> data[1]));

        // When
        doReturn(gmResponse).when(restTemplate).getForObject(anyString(), eq(GMResponse.class));
        Map<String, String> mapResults = elevationService.getAltitude("anyPosition");

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isNotEmpty();
        assertThat(mapResults).isEqualTo(expectedResult);
    }

    @Test
    public void getAltitudePositionsEmptyTest() {
        // Given

        // When
        Map<String, String> mapResults = elevationService.getAltitude("");

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isEmpty();
    }

    @Test
    public void getAltitudePositionsNullTest() {
        // Given

        // When
        Map<String, String> mapResults = elevationService.getAltitude(null);

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isEmpty();
    }

    @Test
    public void createPositionsRequestTrackPointListNullTest() {
        // Given

        // When
        String elevationCode = elevationService.createPositionsRequest(null);

        // Then
        Assertions.assertThat(elevationCode).isNull();
    }

    @Test
    public void createPositionsRequestTrackPointListEmptyTest() {
        // Given
        List<TrackPoint> trackPointList = Collections.emptyList();

        // When
        String elevationCode = elevationService.createPositionsRequest(trackPointList);

        // Then
        Assertions.assertThat(elevationCode).isNull();
    }

    @Test
    public void createPositionsRequestTest() {
        // Given
        BigDecimal latitude = new BigDecimal("5.63654");
        BigDecimal longitude = new BigDecimal("1.3456");
        TrackPoint trackPoint1 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        TrackPoint trackPoint2 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        TrackPoint trackPoint3 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        List<TrackPoint> trackPointList = Stream.of(trackPoint1, trackPoint2, trackPoint3).collect(Collectors.toList());

        // When
        String elevationCode = elevationService.createPositionsRequest(trackPointList);

        // Then
        Assertions.assertThat(elevationCode).isNotNull();
        Assertions.assertThat(elevationCode).isNotEmpty();
        Assertions.assertThat(elevationCode).isEqualTo("5.63654,1.3456|5.63654,1.3456|5.63654,1.3456");
    }

    @Test
    public void createPositionsRequestSomeTrackPointNotHavePositionTest() {
        // Given
        BigDecimal latitude = new BigDecimal("5.63654");
        BigDecimal longitude = new BigDecimal("1.3456");
        TrackPoint trackPoint1 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        TrackPoint trackPoint2 = TrackPoint.builder()
                .build();
        TrackPoint trackPoint3 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        List<TrackPoint> trackPointList = Stream.of(trackPoint1, trackPoint2, trackPoint3).collect(Collectors.toList());

        // When
        String elevationCode = elevationService.createPositionsRequest(trackPointList);

        // Then
        Assertions.assertThat(elevationCode).isNotNull();
        Assertions.assertThat(elevationCode).isNotEmpty();
        Assertions.assertThat(elevationCode).isEqualTo("5.63654,1.3456|5.63654,1.3456");
    }

    @Test
    public void createPositionsRequestSomeTrackPointHaveAltitudeTest() {
        // Given
        BigDecimal latitude = new BigDecimal("5.63654");
        BigDecimal longitude = new BigDecimal("1.3456");
        TrackPoint trackPoint1 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        TrackPoint trackPoint2 = TrackPoint.builder()
                .altitudeMeters(new BigDecimal("440"))
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        TrackPoint trackPoint3 = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();
        List<TrackPoint> trackPointList = Stream.of(trackPoint1, trackPoint2, trackPoint3).collect(Collectors.toList());

        // When
        String elevationCode = elevationService.createPositionsRequest(trackPointList);

        // Then
        Assertions.assertThat(elevationCode).isNotNull();
        Assertions.assertThat(elevationCode).isNotEmpty();
        Assertions.assertThat(elevationCode).isEqualTo("5.63654,1.3456|5.63654,1.3456");
    }

    @Test
    public void getCoordinatesCodeTest() {
        // Given
        BigDecimal latitude = new BigDecimal("5.63654");
        BigDecimal longitude = new BigDecimal("1.3456");
        TrackPoint trackPoint = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(longitude)
                        .build())
                .build();

        // When
        String elevationCode = elevationService.getCoordinatesCode(trackPoint);

        // Then
        Assertions.assertThat(elevationCode).isNotNull();
        Assertions.assertThat(elevationCode).isNotEmpty();
        Assertions.assertThat(elevationCode).isEqualTo("5.63654,1.3456");

    }

    @Test
    public void getCoordinatesCodeNullTrackPointTest() {
        // Given
        // When
        String elevationCode = elevationService.getCoordinatesCode(null);

        // Then
        Assertions.assertThat(elevationCode).isNull();

    }

    @Test
    public void getCoordinatesCodeNullLatitude() {
        // Given
        BigDecimal longitude = new BigDecimal("1.3456");
        TrackPoint trackPoint = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(null)
                        .longitudeDegrees(longitude)
                        .build())
                .build();

        // When
        String elevationCode = elevationService.getCoordinatesCode(trackPoint);

        // Then
        Assertions.assertThat(elevationCode).isNull();

    }

    @Test
    public void getCoordinatesCodeNullLongitudeTest() {
        // Given
        BigDecimal latitude = new BigDecimal("5.63654");
        TrackPoint trackPoint = TrackPoint.builder()
                .position(Position.builder()
                        .latitudeDegrees(latitude)
                        .longitudeDegrees(null)
                        .build())
                .build();

        // When
        String elevationCode = elevationService.getCoordinatesCode(trackPoint);

        // Then
        Assertions.assertThat(elevationCode).isNull();

    }

    @Test
    public void getCoordinatesCodeNullPositionTest() {
        // Given
        TrackPoint trackPoint = TrackPoint.builder()
                .position(null)
                .build();

        // When
        String elevationCode = elevationService.getCoordinatesCode(trackPoint);

        // Then
        Assertions.assertThat(elevationCode).isNull();

    }
}