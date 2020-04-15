package com.routeanalyzer.api.common;

import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

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

    public static double round(final double number, final int round) {
        double roundNumber = Math.pow(10, round);
        return Math.round(number * roundNumber) / roundNumber;
    }

    public static double degrees2Radians(final BigDecimal degrees) {
        return degrees.doubleValue() * Math.PI / 180.0;
    }

    public static Integer increaseUnit(final Integer num) {
        return num + 1;
    }

    public static Integer decreaseUnit(final Integer num) {
        return num - 1;
    }

    // Logical operations

    public static boolean isPositiveNonZero(final Double value) {
        return value > 0;
    }

    public static boolean isPositiveNonZero(final Integer value) {
        return value > 0;
    }

    public static boolean isPositiveNonZero(final Long value) {
        return value > 0;
    }

    public static boolean isPositiveOrZero(final Integer value) {
        return value >= 0;
    }

    public static boolean isPositiveHeartRate(final HeartRateInBeatsPerMinuteT heartRate) {
        return heartRate.getValue() > 0;
    }

    public static List<Integer> sortingPositiveValues(final Integer indexLeft, final Integer indexRight) {
        return ofNullable(indexLeft)
                .filter(MathUtils::isPositiveOrZero)
                .filter(__ -> isPositiveOrZero(indexRight))
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

    public static double metersBetweenCoordinates(final BigDecimal latP1, final BigDecimal lngP1, final BigDecimal latP2,
                                                  final BigDecimal lngP2) {
        return metersBetweenCoordinates(degrees2Radians(latP1), degrees2Radians(lngP1),
                degrees2Radians(latP2), degrees2Radians(lngP2));
    }

    public static double metersBetweenCoordinates(final double latP1, final double lngP1, final double latP2,
                                                  final double lngP2) {
        // Point P
        double rho1 = EARTHS_RADIUS_METERS * Math.cos(latP1);
        double z1 = EARTHS_RADIUS_METERS * Math.sin(latP1);
        double x1 = rho1 * Math.cos(lngP1);
        double y1 = rho1 * Math.sin(lngP1);

        // Point Q
        double rho2 = EARTHS_RADIUS_METERS * Math.cos(latP2);
        double z2 = EARTHS_RADIUS_METERS * Math.sin(latP2);
        double x2 = rho2 * Math.cos(lngP2);
        double y2 = rho2 * Math.sin(lngP2);

        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cosTheta = dot / (Math.pow(EARTHS_RADIUS_METERS, 2));

        double theta = Math.acos(cosTheta);

        return Math.abs(EARTHS_RADIUS_METERS * theta);
    }
}
