package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.model.Position;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.MathUtils.round;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class PositionOperationsImplTest {

    private PositionOperations positionOperations;

    private Position positionDecimals;
    private Position positionInteger;
    private Position emptyPosition;
    private BigDecimal latDouble;
    private BigDecimal lngDouble;
    private BigDecimal latInteger;
    private BigDecimal lngInteger;
    private Position oviedo;
    private Position madrid;
    private Position park;

    @Before
    public void setUp() {
        positionOperations = new PositionOperationsImpl();
        latDouble = toBigDecimal(6.7);
        lngDouble = toBigDecimal(0.005);
        positionDecimals = toPosition(latDouble, lngDouble).orElse(null);
        latInteger = toBigDecimal("6");
        lngInteger = toBigDecimal("1");
        positionInteger = toPosition(latInteger, lngInteger).orElse(null);
        emptyPosition = Position.builder().build();
        oviedo = toPosition("43.3602900", "-5.8447600");
        madrid = toPosition("40.4165000", "-3.7025600");
        park = toPosition("43.352478", "-5.8501170");
    }

    @Test
    public void isEqualsCoordinatesBigDecimalCreationDouble() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, latDouble, lngDouble);
        assertThat(isEqual).isTrue();
    }

    @Test
    public void isEqualsCoordinatesBigDecimalCreationString() {
        boolean isEqual = positionOperations.isThisPosition(positionInteger, latInteger, lngInteger);
        assertThat(isEqual).isTrue();
    }

    @Test
    public void isNotEqualsCoordinatesBigDecimalCreationDouble() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, lngDouble, latDouble);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsCoordinatesBigDecimalCreationString() {
        boolean isEqual = positionOperations.isThisPosition(positionInteger, lngInteger, latInteger);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(null, lngDouble, latDouble);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLatPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, null, lngDouble);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLngPositionNull() {
        boolean isEqual = positionOperations.isThisPosition(positionDecimals, latDouble, null);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void isNotEqualsLatPositionEmpty() {
        boolean isEqual = positionOperations.isThisPosition(emptyPosition, latDouble, lngDouble);
        assertThat(isEqual).isFalse();
    }

    @Test
    public void calculateDistanceTest() {
        // When
        Double distanceOvdPark = positionOperations.calculateDistance(oviedo, park);
        Double distanceOvdMad = positionOperations.calculateDistance(oviedo, madrid);
        // Then
        assertThat(round(distanceOvdPark, 2)).isEqualTo(970.64);
        assertThat(round(distanceOvdMad, 2)).isEqualTo(372247.30);
    }

    @Test
    public void calculateDistanceSamePoint() {
        // When
        Double distanceOvdOvd = positionOperations.calculateDistance(oviedo, oviedo);
        // Then
        assertThat(round(distanceOvdOvd, 2)).isEqualTo(0.0);
    }

    @Test
    public void calculateDistancePositionsWithoutLatLng() {
        // When
        Double distanceOvdOvd = positionOperations.calculateDistance(Position.builder().build(),
                Position.builder().build());
        // Then
        assertThat(distanceOvdOvd).isNull();
    }
}