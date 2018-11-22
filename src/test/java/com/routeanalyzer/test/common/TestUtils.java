package com.routeanalyzer.test.common;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.core.io.Resource;

import com.mongodb.Function;
import com.routeanalyzer.model.Activity;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {
	
	public static final String FAKE_ID_GPX = "5ace8cd14c147400048aa6b0";
	public static final String FAKE_ID_TCX = "5ace8caf4c147400048aa6af";
	
	public static byte[] getFileBytes(Resource resource) {
		Function<Path, Try<byte[]>> toByteArray = (path) -> Try.of(() -> Files.readAllBytes(path));
		return Optional.ofNullable(Try.of(() -> resource.getFile().toPath()).getOrNull()).map(toByteArray::apply)
				.orElse(null).getOrNull();
	}

	public static Function<Path, BufferedReader> toBufferedReader = (path) -> Try
			.of(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8)).getOrNull();
	
	public static Supplier<Activity> createGPXActivity = () -> {
		return createActivityByXmlType("gpx", FAKE_ID_GPX);
	};
	
	public static Supplier<Activity> createTCXActivity = () -> {
		return createActivityByXmlType("tcx", FAKE_ID_TCX);
	};
	
	public static Supplier<Activity> createUnknownActivity = () -> {
		return new Activity();
	};
	
	private static Activity createActivityByXmlType(String xmlType, String id){
		Activity activity = new Activity();
		activity.setDevice("Garmin Connect");
		activity.setName("Untitled");
		activity.setDate(new Date(1311586432000L));
		activity.setSourceXmlType(xmlType);
		activity.setId(id);
		return activity;
	}
	
}
