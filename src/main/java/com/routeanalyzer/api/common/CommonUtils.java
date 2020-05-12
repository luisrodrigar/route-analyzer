package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@UtilityClass
public class CommonUtils {

	// List Utils

	public static <T> List<T> toListOfType(final List<String> listStrings, final Function<String, T> convertTo) {
		Function<String, Optional<T>> checkConvertTo = str ->  Try.of(() -> convertTo.apply(str))
				.onFailure(err -> log.error("Error trying to convert to a List of types using {}", convertTo, err))
				.toJavaOptional();
		return IntStream.range(0, listStrings.size())
				.boxed()
				.map(listStrings::get)
				.map(checkConvertTo)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	// Boolean utils

	public static <T> Predicate<T> not(final Predicate<T> t) {
		return ofNullable(t)
				.map(Predicate::negate)
				.orElse(null);
	}

	// String utils

	public static String toStringValue(final Object value) {
		return ofNullable(value)
				.map(String::valueOf)
				.orElse(null);
	}

}
