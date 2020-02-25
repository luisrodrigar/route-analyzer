package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.xml.gpx11.WptType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static com.routeanalyzer.api.common.MathUtils.round;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PositionOperationsImplTest {

    private PositionOperations positionOperations;

    private Position positionDecimals;
    private Position positionInteger;
    private Position emptyPosition;
    private String latitude1;
    private String longitude1;
    private BigDecimal latDouble;
    private BigDecimal lngDouble;
    private String latitude2;
    private String longitude2;
    private BigDecimal latInteger;
    private BigDecimal lngInteger;
    private Position oviedo;
    private Position madrid;
    private Position park;

    @Before
    public void setUp() {
        positionOperations = new PositionOperationsImpl();
        latitude1 = "6.7";
        longitude1 = "0.005";
        latDouble = new BigDecimal(latitude1);
        lngDouble = new BigDecimal(longitude1);
        positionDecimals = Position.builder()
                .latitudeDegrees(latDouble)
                .longitudeDegrees(lngDouble)
                .build();
        latitude2 = "6";
        longitude2 = "1";
        latInteger = new BigDecimal(latitude2);
        lngInteger = new BigDecimal(longitude2);
        positionInteger = Position.builder()
                .latitudeDegrees(latInteger)
                .longitudeDegrees(lngInteger)
                .build();
        emptyPosition = Position.builder().build();
        oviedo = Position.builder()
                .latitudeDegrees(new BigDecimal("43.3602900"))
                .longitudeDegrees(new BigDecimal("-5.8447600"))
                .build();
        madrid = Position.builder()
                .latitudeDegrees(new BigDecimal("40.4165000"))
                .longitudeDegrees(new BigDecimal("-3.7025600"))
                .build();
        park = Position.builder()
                .latitudeDegrees(new BigDecimal("43.352478"))
                .longitudeDegrees(new BigDecimal("-5.8501170"))
                .build();
    }

    @Test
    public void isEqualsCoordinatesBigDecimalCreationDouble() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, latitude1, longitude1);
        assertThat(isEqual).isTrue();
    }

    @Test
    public void isEqualsCoordinatesBigDecimalCreationString() {
        boolean isEqual = positionOperations.isThisPosition(positionInteger, latitude2, longitude2);
        assertThat(isEqual).isTrue();
    }

    @Test
    public void isNotEqualsCoordinatesBigDecimalCreationDouble() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, longitude1, latitude1);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsCoordinatesBigDecimalCreationString() {
        boolean isEqual = positionOperations.isThisPosition(positionInteger, longitude2, latitude2);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(null, longitude1, latitude1);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLatPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, null, longitude1);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLngPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, latitude1, null);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLatPositionEmpty() {
        boolean isEqual = positionOperations.isThisPosition(emptyPosition, latitude1, longitude1);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void calculateDistanceTest() {
        // When
        Optional<Double> distanceOvdPark = positionOperations.calculateDistance(oviedo, park);
        Optional<Double> distanceOvdMad = positionOperations.calculateDistance(oviedo, madrid);
        // Then
        assertThat(distanceOvdPark).isNotEmpty();
        assertThat(distanceOvdMad).isNotEmpty();
        assertThat(round(distanceOvdPark.get(), 2)).isEqualTo(970.64);
        assertThat(round(distanceOvdMad.get(), 2)).isEqualTo(372247.30);
    }

    @Test
    public void calculateDistanceSamePoint() {
        // When
        Optional<Double> distanceOvdOvd = positionOperations.calculateDistance(oviedo, oviedo);
        // Then
        assertThat(distanceOvdOvd).isNotEmpty();
        assertThat(round(distanceOvdOvd.get(), 2)).isEqualTo(0.0);
    }

    @Test
    public void calculateDistancePositionsWithoutLatLng() {
        // When
        Optional<Double> distanceOvdOvd = positionOperations.calculateDistance(Position.builder().build(),
                Position.builder().build());
        // Then
        assertThat(distanceOvdOvd).isEmpty();
    }

    @Test
    public void toPositionByCoordinates() {
        // Given
        String latitude = "42.6132120";
        String longitude = "-6.5734430";

        // When
        Position result = positionOperations.toPosition(latitude, longitude);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitudeDegrees()).isEqualTo(new BigDecimal(latitude));
        assertThat(result.getLongitudeDegrees()).isEqualTo(new BigDecimal(longitude));
    }

    @Test
    public void toPositionByNullLatitudeCoordinates() {
        // Given
        String longitude = "-6.5734430";

        // When
        Optional<Position> result = positionOperations.toOptPosition(null, longitude);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void toPositionByNullLongitudeCoordinates() {
        // Given
        String latitude = "42.6132120";

        // When
        Optional<Position> result = positionOperations.toOptPosition(latitude, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void toOptPositionByCoordinates() {
        // Given
        String latitude = "42.6132120";
        String longitude = "-6.5734430";

        // When
        Optional<Position> result = positionOperations.toOptPosition(latitude, longitude);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getLatitudeDegrees()).isEqualTo(new BigDecimal(latitude));
        assertThat(result.get().getLongitudeDegrees()).isEqualTo(new BigDecimal(longitude));
    }

    @Test
    public void toPositionByDoubleCoordinates() {
        // Given
        double latitude = 42.6132120;
        double longitude = -6.5734430;

        // When
        Position result = positionOperations.toPosition(latitude, longitude);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitudeDegrees())
                .isEqualTo(new BigDecimal(String.valueOf(latitude)));
        assertThat(result.getLongitudeDegrees())
                .isEqualTo(new BigDecimal(String.valueOf(longitude)));
    }

    @Test
    public void toPositionByBigDecimalCoordinates() {
        // Given
        BigDecimal latitude = new BigDecimal("42.6132120");
        BigDecimal longitude = new BigDecimal("-6.5734430");

        // When
        Optional<Position> result = positionOperations.toPosition(latitude, longitude);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getLatitudeDegrees()).isEqualTo(latitude);
        assertThat(result.get().getLongitudeDegrees()).isEqualTo(longitude);
    }

    @Test
    public void toPositionByWayPoint() {
        // Given
        BigDecimal latitude = new BigDecimal("42.6132120");
        BigDecimal longitude = new BigDecimal("-6.5734430");
        WptType wptType = new WptType();
        wptType.setLat(latitude);
        wptType.setLon(longitude);

        // When
        Optional<Position> result = positionOperations.toPosition(wptType);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getLatitudeDegrees()).isEqualTo(latitude);
        assertThat(result.get().getLongitudeDegrees()).isEqualTo(longitude);
    }
}
