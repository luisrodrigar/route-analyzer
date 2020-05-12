package com.routeanalyzer.api.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class CommonUtilsTest {

    @Test
    public void convertToListBoolean() {
        // Given
        List<String> inputList = Arrays.asList("true","false", "true","true");
        // When
        List<Boolean> result = CommonUtils.toListOfType(inputList, Boolean::valueOf);
        // Then
        assertThat(result).isEqualTo(Arrays.asList(true, false,
                true, true));
    }

    @Test
    public void convertToListLong() {
        // Given
        List<String> inputList = Arrays.asList("8321312431412412422",
                "1043123412312312313", "4531231231233232",
                "32312312314231394");
        // When
        List<Long> result = CommonUtils.toListOfType(inputList, Long::valueOf);
        // Then
        assertThat(result)
                .isEqualTo(Arrays.asList(8321312431412412422L,
                        1043123412312312313L, 4531231231233232L,
                        32312312314231394L));
    }

    @Test
    public void convertEmptyList() {
        // Given
        List<String> inputList = Collections.emptyList();
        // When
        List<Long> result = CommonUtils.toListOfType(inputList, Long::valueOf);
        // Then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    public void convertToNullList() {
        // Given
        // When
        List<Long> result = CommonUtils.toListOfType(null, Long::valueOf);
        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testPredicateNotIsEmpty() {
        // Given
        Predicate<List> isEmpty = list -> list.isEmpty();
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);

        // When
        boolean result = CommonUtils.not(isEmpty).test(integers);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void testPredicateNotWhenIsNull() {
        // Given
        Predicate<List> predicateNull = null;
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);

        // When
        Predicate<List> result = CommonUtils.not(predicateNull);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testObjectToString() {
        // Given
        Object string = "Hello, tester";

        // When
        String result  = CommonUtils.toStringValue(string);

        // Then
        assertThat(result).isInstanceOf(String.class);
    }

    @Test
    public void testObjectNonStringToString() {
        // Given
        Object number = 8L;

        // When
        String result  = CommonUtils.toStringValue(number);

        // Then
        assertThat(result).isInstanceOf(String.class);
    }

    @Test
    public void testConvertNullToString() {
        // Given
        Object isNullObject = null;

        // When
        String result  = CommonUtils.toStringValue(isNullObject);

        // Then
        assertThat(result).isNull();
    }
}
