package com.routeanalyzer.api.common;

import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.HeartRateValueT;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MathUtilsTest {

    @Test
    public void doubleToBigDecimal() {
        // Given
        double input = 1234.56;

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isEqualTo(new BigDecimal(input));
    }

    @Test
    public void doubleNonFiniteToBigDecimal() {
        // Given
        Double input = Double.POSITIVE_INFINITY;

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void nullDoubleToBigDecimal() {
        // Given
        Double input = null;

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void stringToBigDecimal() {
        // Given
        String input = "1234.56";

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isEqualTo(new BigDecimal(input));
    }

    @Test
    public void nullStringToBigDecimal() {
        // Given
        String input = null;

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void emptyStringToBigDecimal() {
        // Given
        String input = "";

        // When
        BigDecimal result = MathUtils.toBigDecimal(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void roundNullNumber() {
        // Given
        Double input = null;

        // When
        Double result = MathUtils.round(input, 2);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void roundNullRound() {
        // Given
        Integer round = null;

        // When
        Double result = MathUtils.round(3.4566, round);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void round() {
        // Given
        Integer round = null;

        // When
        Double result = MathUtils.round(3.4566, 2);

        // Then
        assertThat(result).isEqualTo(3.46);
    }

    @Test
    public void round0Decimals() {
        // Given
        Integer round = null;

        // When
        Double result = MathUtils.round(3.4566, 0);

        // Then
        assertThat(result).isEqualTo(3.0);
    }

    @Test
    public void roundDecimalsGreaterThanDecimals() {
        // Given
        Integer round = null;

        // When
        Double result = MathUtils.round(3.4566, 6);

        // Then
        assertThat(result).isEqualTo(3.4566);
    }


    @Test
    public void degrees2Radians() {
        // Given
        BigDecimal madLatitude = new BigDecimal("40.4165000");

        // When
        Double radians = MathUtils.degrees2Radians(madLatitude);

        // Then
        assertThat(radians).isEqualTo(.7054009971322882);
    }

    @Test
    public void nullDegrees2Radians() {
        // Given

        // When
        Double radians = MathUtils.degrees2Radians(null);

        // Then
        assertThat(radians).isNull();
    }

    @Test
    public void increaseUnit() {
        // Given
        Integer index = 0;

        // When
        Integer result = MathUtils.increaseUnit(index);

        // Then
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void nullIncreaseUnit() {
        // Given

        // When
        Integer result = MathUtils.increaseUnit(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void decreaseUnit() {
        // Given
        Integer index = 1;

        // When
        Integer result = MathUtils.decreaseUnit(index);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void nullDecreaseUnit() {
        // Given

        // When
        Integer result = MathUtils.decreaseUnit(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void isPositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(0.3);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isZeroPositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(0.0);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void isNegativeValuePositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(-2.34);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void isNullValuePositiveNonZero() {
        // Given
        Double nullValue = null;

        // When
        Boolean result = MathUtils.isPositiveNonZero(nullValue);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testZeroIsPositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(0);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testPositiveIsPositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(2);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void testNegativeIsPositiveNonZero() {
        // Given

        // When
        boolean result = MathUtils.isPositiveNonZero(-2);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testNullIsPositiveNonZero() {
        // Given
        Integer nullValue = null;

        // When
        Boolean result = MathUtils.isPositiveNonZero(nullValue);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void isZeroPositiveOrZero() {
        // Given

        // When
        Boolean result = MathUtils.isPositiveOrZero(0);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isPositiveOrZero() {
        // Given

        // When
        Boolean result = MathUtils.isPositiveOrZero(23);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isNegativeValuePositiveOrZero() {
        // Given

        // When
        Boolean result = MathUtils.isPositiveOrZero(-3);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void isNullValuePositiveOrZero() {
        // Given

        // When
        Boolean result = MathUtils.isPositiveOrZero(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void isPositiveHeartRate() {
        // Given
        HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = new HeartRateInBeatsPerMinuteT();
        heartRateInBeatsPerMinuteT.setValue((short)175);
        // When
        Boolean result = MathUtils.isPositiveHeartRate(heartRateInBeatsPerMinuteT);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void isZeroHeartRatePositiveHeartRate() {
        // Given
        HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = new HeartRateInBeatsPerMinuteT();
        heartRateInBeatsPerMinuteT.setValue((short)0);
        // When
        Boolean result = MathUtils.isPositiveHeartRate(heartRateInBeatsPerMinuteT);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void isNegativeHeartRatePositiveHeartRate() {
        // Given
        HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = new HeartRateInBeatsPerMinuteT();
        heartRateInBeatsPerMinuteT.setValue((short)-78);
        // When
        Boolean result = MathUtils.isPositiveHeartRate(heartRateInBeatsPerMinuteT);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void isNullHeartRateObjectPositiveHeartRate() {
        // Given

        // When
        Boolean result = MathUtils.isPositiveHeartRate(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void sortingUnorderedPositiveValues() {
        // Given

        // When
        List<Integer> result = MathUtils.sortingPositiveValues(2, 0);

        // Then
        assertThat(result).isEqualTo(Arrays.asList(0,2));
    }

    @Test
    public void sortingOrderedPositiveValues() {
        // Given

        // When
        List<Integer> result = MathUtils.sortingPositiveValues(1, 3);

        // Then
        assertThat(result).isEqualTo(Arrays.asList(1,3));
    }

    @Test
    public void sortingFirstNullParamValues() {
        // Given

        // When
        List<Integer> result = MathUtils.sortingPositiveValues(null, 0);

        // Then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    public void sortingSecondNullParamValues() {
        // Given

        // When
        List<Integer> result = MathUtils.sortingPositiveValues(0, null);

        // Then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    public void metersBetweenCoordinates() {
        // Given
        String longitudeOviedo = "-5.8447600";
        String latitudeOvideo = "43.3602900";

        String longitudeMadrid = "-3.7025600";
        String latitudeMadrid = "40.4165000";

        BigDecimal latOviedo = new BigDecimal(latitudeOvideo);
        BigDecimal lngOviedo = new BigDecimal(longitudeOviedo);

        BigDecimal latMadrid = new BigDecimal(latitudeMadrid);
        BigDecimal lngMadrid = new BigDecimal(longitudeMadrid);


        // When
        Double result = MathUtils.metersBetweenCoordinates(latOviedo, lngOviedo, latMadrid, lngMadrid);

        // Then
        assertThat(result).isEqualTo(372247.29576206073);
    }

    @Test
    public void metersBetweenCoordinatesNullFirstParameter() {
        // Given
        String longitudeOviedo = "-5.8447600";

        String longitudeMadrid = "-3.7025600";
        String latitudeMadrid = "40.4165000";

        BigDecimal latOviedo = null;
        BigDecimal lngOviedo = new BigDecimal(longitudeOviedo);

        BigDecimal latMadrid = new BigDecimal(latitudeMadrid);
        BigDecimal lngMadrid = new BigDecimal(longitudeMadrid);


        // When
        Double result = MathUtils.metersBetweenCoordinates(latOviedo, lngOviedo, latMadrid, lngMadrid);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void metersBetweenCoordinatesNullSecondParameter() {
        // Given
        String latitudeOvideo = "43.3602900";

        String longitudeMadrid = "-3.7025600";
        String latitudeMadrid = "40.4165000";

        BigDecimal latOviedo = new BigDecimal(latitudeOvideo);
        BigDecimal lngOviedo = null;

        BigDecimal latMadrid = new BigDecimal(latitudeMadrid);
        BigDecimal lngMadrid = new BigDecimal(longitudeMadrid);


        // When
        Double result = MathUtils.metersBetweenCoordinates(latOviedo, lngOviedo, latMadrid, lngMadrid);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void metersBetweenCoordinatesNullThirdParameter() {
        // Given
        String latitudeOvideo = "43.3602900";
        String longitudeOviedo = "-5.8447600";

        String longitudeMadrid = "-3.7025600";
        String latitudeMadrid = "40.4165000";

        BigDecimal latOviedo = new BigDecimal(latitudeOvideo);
        BigDecimal lngOviedo = new BigDecimal(longitudeOviedo);

        BigDecimal latMadrid = null;
        BigDecimal lngMadrid = new BigDecimal(longitudeMadrid);


        // When
        Double result = MathUtils.metersBetweenCoordinates(latOviedo, lngOviedo, latMadrid, lngMadrid);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void metersBetweenCoordinatesNullForthParameter() {
        // Given
        String latitudeOvideo = "43.3602900";
        String longitudeOviedo = "-5.8447600";

        String longitudeMadrid = "-3.7025600";
        String latitudeMadrid = "40.4165000";

        BigDecimal latOviedo = new BigDecimal(latitudeOvideo);
        BigDecimal lngOviedo = new BigDecimal(longitudeOviedo);

        BigDecimal latMadrid = new BigDecimal(latitudeMadrid);
        BigDecimal lngMadrid = null;


        // When
        Double result = MathUtils.metersBetweenCoordinates(latOviedo, lngOviedo, latMadrid, lngMadrid);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMetersBetweenCoordinates() {
        // Given
        double longitudeOviedo = -0.102010306;
        double latitudeOvideo = 0.75677982512;

        double longitudeMadrid = -0.06462186275;
        double latitudeMadrid = 0.7054009971;


        // When
        Double result = MathUtils.metersBetweenRadiansCoordinates(latitudeOvideo, longitudeOviedo,
                latitudeMadrid, longitudeMadrid);

        // Then
        assertThat(result).isEqualTo(372247.29597759416);
    }
}
