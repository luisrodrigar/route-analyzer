package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@UtilityClass
public class CommonUtils {

	// List Utils

	public static <T> List<T> toListOfType(final List<String> listStrings, final Function<String, T> convertTo) {
		return ofNullable(listStrings)
				.filter(__ -> nonNull(convertTo))
				.map(__ -> listStrings
						.stream()
						.flatMap(stringObject -> Try.of(() -> convertTo.apply(stringObject))
								.onFailure(err -> log.error("Error trying to convert " +
										"to a List of types using {}", convertTo, err))
								.toJavaStream())
						.collect(toList()))
				.orElse(null);
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
