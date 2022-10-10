package com.routeanalyzer.api.common;

import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.Objects.nonNull;

@UtilityClass
public class MathUtils {
    // Radius of earth in meters
    public static final double EARTHS_RADIUS_METERS = 6371000.0;

    // Mathematics operations

    public static BigDecimal toBigDecimal(final String number) {
        return ofNullable(number)
                .filter(StringUtils::isNotEmpty)
                .map(BigDecimal::new)
                .orElse(null);
    }

    public static BigDecimal toBigDecimal(final Double number) {
        return ofNullable(number)
                .filter(Double::isFinite)
                .map(BigDecimal::new)
                .orElse(null);
    }

    public static Double round(final Double number, final Integer round) {
        return ofNullable(number)
                .flatMap(__ -> ofNullable(round)
                        .map(___ ->  Math.pow(10, round))
                        .map(rounder -> Math.round(number * rounder) / rounder))
                .orElse(null);
    }

    public static Double degrees2Radians(final BigDecimal degrees) {
        return ofNullable(degrees)
                .map(BigDecimal::doubleValue)
                .map(degreesDouble -> degreesDouble * Math.PI / 180.0)
                .orElse(null);
    }

    public static Integer increaseUnit(final Integer num) {
        return ofNullable(num)
                .map(__ -> num + 1)
                .orElse(null);
    }

    public static Integer decreaseUnit(final Integer num) {
        return ofNullable(num)
                .map(__ -> num - 1)
                .orElse(null);
    }

    // Logical operations

    public static Boolean isPositiveNonZero(final Double value) {
        return ofNullable(value)
                .map(__ -> value > 0)
                .orElse(null);
    }

    public static Boolean isPositiveNonZero(final Integer value) {
        return ofNullable(value)
                .map(__ -> value > 0)
                .orElse(null);
    }

    public static Boolean isPositiveOrZero(final Integer value) {
        return ofNullable(value)
                .map(__ -> value >= 0)
                .orElse(null);
    }

    public static Boolean isPositiveHeartRate(final HeartRateInBeatsPerMinuteT heartRate) {
        return ofNullable(heartRate)
                .map(HeartRateInBeatsPerMinuteT::getValue)
                .map(heartRateValue -> heartRateValue > 0)
                .orElse(null);
    }

    public static List<Integer> sortingPositiveValues(final Integer indexLeft, final Integer indexRight) {
        return ofNullable(indexLeft)
                .filter(MathUtils::isPositiveOrZero)
                .filter(__ -> ofNullable(isPositiveOrZero(indexRight))
                        .orElse(false))
                .map(__ -> indexLeft.compareTo(indexRight) > 0
                        ? swapValues(indexLeft, indexRight) : asList(indexLeft, indexRight))
                .orElse(emptyList());
    }

    private static List<Integer> swapValues(Integer smallerNumber, Integer biggerNumber) {
        smallerNumber = smallerNumber + biggerNumber;
        biggerNumber = smallerNumber - biggerNumber;
        smallerNumber = smallerNumber - biggerNumber;
        return asList(smallerNumber, biggerNumber);
    }

    public static Double metersBetweenCoordinates(final BigDecimal latP1, final BigDecimal lngP1, final BigDecimal latP2,
                                                  final BigDecimal lngP2) {
        return ofNullable(latP1)
                .filter(__ -> nonNull(latP2))
                .filter(__ -> nonNull(lngP1))
                .filter(__ -> nonNull(lngP2))
                .map(__ -> metersBetweenRadiansCoordinates(
                        degrees2Radians(latP1),
                        degrees2Radians(lngP1),
                        degrees2Radians(latP2),
                        degrees2Radians(lngP2)))
                .orElse(null);
    }

    public static double metersBetweenRadiansCoordinates(final double latP1, final double lngP1, final double latP2,
                                                  final double lngP2) {
        double dLon = lngP2 - lngP1;
        double dLat = latP2 - latP1;

        // Haversine formula
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(latP1) * Math.cos(latP2)
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return Math.abs(EARTHS_RADIUS_METERS * c);
    }
}
