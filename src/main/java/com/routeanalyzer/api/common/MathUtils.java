package com.routeanalyzer.api.common;

import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@UtilityClass
public class MathUtils {
    // Radius of earth in meters
    public static final double EARTHS_RADIUS_METERS = 6371000.0;

    /**
     * Mathematic operations
     */

    public static Optional<BigDecimal> toBigDecimal(String number) {
        return ofNullable(number).filter(StringUtils::isNotEmpty).map(BigDecimal::new);
    }

    public static BigDecimal toBigDecimal(Double number) {
        return ofNullable(number).map(BigDecimal::new).orElse(null);
    }

    public static double round(double number, int round) {
        double roundNumber = Math.pow(10, round);
        return Math.round(number * roundNumber) / roundNumber;
    }

    public static double degrees2Radians(BigDecimal degrees) {
        return degrees.doubleValue() * Math.PI / 180.0;
    }

    public static Integer increaseUnit(Integer num) {
        return num + 1;
    }

    public static Integer decreaseUnit(Integer num) {
        return num - 1;
    }

    /**
     * Logical operations
     */

    public static boolean isPositiveNonZero(Double value) {
        return value > 0;
    }

    public static boolean isPositiveNonZero(Integer value) {
        return value > 0;
    }

    public static boolean isPositiveNonZero(Long value) {
        return value > 0;
    }

    public static boolean isPositiveOrZero(Integer value) {
        return value >= 0;
    }

    public static boolean isPositiveHeartRate(HeartRateInBeatsPerMinuteT heartRate) {
        return heartRate.getValue() > 0;
    }

    public static Integer[] swappingValues(Integer num1, Integer num2) {
        num1 = num1 * num2;
        num2 = num1 / num2;
        num1 = num1 / num2;
        return new Integer[]{num1, num2};
    }

    /**
     *
     */
    public static double metersBetweenCoordinates(double latP1, double lngP1, double latP2, double lngP2) {
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

        return EARTHS_RADIUS_METERS * theta;
    }
}
