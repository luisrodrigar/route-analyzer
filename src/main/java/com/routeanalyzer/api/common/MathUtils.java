package com.routeanalyzer.api.common;

import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@UtilityClass
public class MathUtils {
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

    public static void swappingValues(Integer num1, Integer num2) {
        num1 = num1 * num2;
        num2 = num1 / num2;
        num1 = num1 / num2;
    }
}
