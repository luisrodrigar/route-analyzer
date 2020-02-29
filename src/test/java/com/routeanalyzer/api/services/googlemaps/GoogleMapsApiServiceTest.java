package com.routeanalyzer.api.services.googlemaps;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.googlemaps.model.GoggleMapsAPIResponse;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.wiremock.WireMockSpring.options;
import static utils.TestUtils.getFileBytes;

@RunWith(MockitoJUnitRunner.class)
public class GoogleMapsApiServiceTest {

    private static final String LOCALHOST_HOST_NAME = "localhost";
    private static final String ELEVATION_ENDPOINT = "/maps/api/elevation/json?locations=%s&key=%s";
    private static final String API_KEY = "BIzeSu0Etz13LA1c031BFUeuNsRZ1xO4uYiM0fB";
    private static final String ELEVATION_STUBBING_RESPONSE = "stubbing-googlemaps-elevations-response.json";
    private static final String ELEVATION_STUBBING_ERROR_RESPONSE = "stubbing-googlemaps-elevations-error.json";
    private static final String POSITIONS = "7.3212,1.3419|4.3212,2.3419|3.6012,0.3419";
    private static final String POSITIONS_URL_ENCODED = "7.3212,1.3419%7C4.3212,2.3419%7C3.6012,0.3419";
    private static final String DATA_RESOURCE = "expected/googlemaps/result-elevations-map.json";
    private static final String ERROR_DATA_RESOURCE = "expected/googlemaps/result-elevations-error.json";

    @Rule
    public WireMockClassRule googleApiWireMock = new WireMockClassRule(options()
            .dynamicPort()
            .bindAddress(LOCALHOST_HOST_NAME));

    @InjectMocks
    private GoogleMapsApiService elevationService;
    @Mock
    private GoogleMapsApiProperties elevationsProperties;
    @Spy
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private void mockElevationProperties() {
        when(elevationsProperties.getElevationHost()).thenReturn(getGoogleMapsElevationUrl());
        when(elevationsProperties.getElevationEndpoint()).thenReturn("/maps/api/elevation/json");
        when(elevationsProperties.getElevationProtocol()).thenReturn("http");
        when(elevationsProperties.getApiKey()).thenReturn(API_KEY);
    }

    @Before
    public void setUp() {
        mockElevationProperties();
    }

    private String getGoogleMapsElevationUrl() {
        return format("%s:%d", LOCALHOST_HOST_NAME, googleApiWireMock.port());
    }

    private String getEndpoint(String positions) {
        return format(ELEVATION_ENDPOINT, positions, API_KEY);
    }

    @Test
    public void getAltitudeTest() {
        // Given
        stubFor(get(urlEqualTo(getEndpoint(POSITIONS_URL_ENCODED)))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile(ELEVATION_STUBBING_RESPONSE)));
        Map expectedResult = Try.of(() -> objectMapper.readValue(getFileBytes(DATA_RESOURCE), Map.class)).get();

        // When
        Map<String, String> mapResults = elevationService.getAltitude(POSITIONS);

        // Then
        assertThat(mapResults).isNotEmpty();
        assertThat(mapResults).isEqualTo(expectedResult);
        verify(restTemplate).getForObject(
                "http://localhost:" + googleApiWireMock.port() + getEndpoint(POSITIONS),
                GoggleMapsAPIResponse.class);
    }

    @Test
    public void getAltitudeFailServiceTest() {
        // Given
        stubFor(get(urlEqualTo(getEndpoint(POSITIONS_URL_ENCODED)))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile(ELEVATION_STUBBING_ERROR_RESPONSE)));
        Map errorResult = Try.of(() -> objectMapper.readValue(getFileBytes(ERROR_DATA_RESOURCE), Map.class)).get();

        // When
        Map<String, String> mapResults = elevationService.getAltitude(POSITIONS);

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isNotEmpty();
        assertThat(mapResults).isEqualTo(errorResult);
        verify(restTemplate).getForObject(
                "http://localhost:" + googleApiWireMock.port() + getEndpoint(POSITIONS),
                GoggleMapsAPIResponse.class);
    }

    @Test
    public void getAltitudePositionsEmptyTest() {
        // Given

        // When
        Map<String, String> mapResults = elevationService.getAltitude("");

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isEmpty();
        verify(restTemplate, never()).getForObject(any(),any());
    }

    @Test
    public void getAltitudePositionsNullTest() {
        // Given

        // When
        Map<String, String> mapResults = elevationService.getAltitude(null);

        // Then
        assertThat(mapResults).isNotNull();
        assertThat(mapResults).isEmpty();
        verify(restTemplate, never()).getForObject(any(),any());
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
