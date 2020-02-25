package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class CommonUtils {

	// List utils

	public static <T> T getFirstElement(List<T> list) {
		return ofNullable(list)
				.map(listParam -> listParam.get(0))
				.orElse(null);
	}

	// Boolean utils

	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	// Response utils

	public static ResponseEntity<String> createOKApplicationOctetResponse(String file) {
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(file);
	}

	// String utils

	public static <T> List<T> toListOfType(List<String> listStrings, Function<String, T> convertTo) {
		Function<String, T> checkConvertTo = str ->  Try.of(() -> convertTo.apply(str))
				.getOrElseThrow(() -> new IllegalArgumentException(format("Could not be " +
						"convert string: %s to type", str)));
		return IntStream.range(0, listStrings.size())
				.boxed()
				.map(index -> checkConvertTo.apply(listStrings.get(index)))
				.collect(toList());
	}

	public static String toStringValue(Object value) {
		return ofNullable(value)
				.map(String::valueOf)
				.orElse(null);
	}

	public static String joinByComma(Object firstParam, Object secondParam) {
		return format("%s%s%s", firstParam, COMMA_DELIMITER, secondParam);
	}

}
