package com.routeanalyzer.test.common;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.core.io.Resource;
import com.mongodb.Function;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;

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
		Activity activity = createActivityByXmlType("tcx", FAKE_ID_TCX);
		Lap lap1 = createLap(98.6231732776618, 4.112047870706494, 103, 6.143949288527114, 16.0, 43.34577092311236, 1,
				1519737373000L);
		lap1.addTrack(new TrackPoint(new Date(1519737373000L), 1,
				new Position(new BigDecimal("42.6131970"), new BigDecimal("-6.5732170")), new BigDecimal("557.3"),
				new BigDecimal("0"), new BigDecimal("0"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737376000L), 2,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5733020")), new BigDecimal("557.3"),
				new BigDecimal("7.16095400022874173373566009104251861572265625"), new BigDecimal("2.386984666742913763215483413659967482089996337890625"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737378000L), 3,
				new Position(new BigDecimal("42.6132170"), new BigDecimal("-6.5733730")), new BigDecimal("557.3"),
				new BigDecimal("13.005015472558941524994224891997873783111572265625"), new BigDecimal("2.9220307361650998956292824004776775836944580078125"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737380000L), 4,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5734430")), new BigDecimal("557.3"),
				new BigDecimal("18.76735956551630835065225255675613880157470703125"), new BigDecimal("2.8811720464786834128290138323791325092315673828125"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737383000L), 5,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5735170")), new BigDecimal("557.3"),
				new BigDecimal("24.829908844772916154397535137832164764404296875"), new BigDecimal("2.02084975975220260124842752702534198760986328125"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737384000L), 6,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5735920")), new BigDecimal("557.3"),
				new BigDecimal("30.973858133300030459622576017864048480987548828125"), new BigDecimal("6.143949288527114305225040880031883716583251953125"), 96));
		lap1.addTrack(new TrackPoint(new Date(1519737386000L), 7,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5736650")), new BigDecimal("557.3"),
				new BigDecimal("36.95465469202076036481230403296649456024169921875"), new BigDecimal("2.9903982793603649525948640075512230396270751953125"), 99));
		lap1.addTrack(new TrackPoint(new Date(1519737389000L), 8,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5737430")), new BigDecimal("557.3"),
				new BigDecimal("43.34577092311236157229359378106892108917236328125"), new BigDecimal("2.130372077030533883856833199388347566127777099609375"), 103));	
		activity.addLap(lap1);
		Lap lap2 = createLap(112.2, 4.112047870706494, 116, 7.053560612105727, 8.0, 28.934607828341, 2, 1519737390000L);
		lap2.addTrack(new TrackPoint(new Date(1519737390000L), 1,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250")), new BigDecimal("557.3"),
				new BigDecimal("50.06350257930233738079550676047801971435546875"), new BigDecimal("6.71773165618997580850191297940909862518310546875"), 106));	
		lap2.addTrack(new TrackPoint(new Date(1519737393000L), 2,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5739120")), new BigDecimal("557.3"),
				new BigDecimal("57.191584795088687087627477012574672698974609375"), new BigDecimal("2.376027405262116420914253467344678938388824462890625"), 109));	
		lap2.addTrack(new TrackPoint(new Date(1519737395000L), 3,
				new Position(new BigDecimal("42.6132170"), new BigDecimal("-6.5739970")), new BigDecimal("557.3"),
				new BigDecimal("64.178236716566146924378699623048305511474609375"), new BigDecimal("3.49332596073872991837561130523681640625"), 115));	
		lap2.addTrack(new TrackPoint(new Date(1519737396000L), 4,
				new Position(new BigDecimal("42.6132200"), new BigDecimal("-6.5740830")), new BigDecimal("557.3"),
				new BigDecimal("71.2317973286718739700518199242651462554931640625"), new BigDecimal("7.0535606121057270456731203012168407440185546875"), 115));	
		lap2.addTrack(new TrackPoint(new Date(1519737398000L), 5,
				new Position(new BigDecimal("42.6132350"), new BigDecimal("-6.5741700")), new BigDecimal("557.3"),
				new BigDecimal("78.5524299205609253249349421821534633636474609375"), new BigDecimal("3.6603162959445256774415611289441585540771484375"), 116));	
		activity.addLap(lap2);
		Lap lap3 = createLap(120.0, 6.717731656189975, 120, 6.717731656189975, 1.0, 0.0, 3, 1519737400000L);
		lap3.addTrack(new TrackPoint(new Date(1519737450000L), 1,
				new Position(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250")), new BigDecimal("557.3"),
				new BigDecimal("0.0"), new BigDecimal("6.71773165618997580850191297940909862518310546875"), 120));	
		activity.addLap(lap3);
		return activity;
	};

	public static Supplier<Activity> createUnknownActivity = () -> {
		return new Activity();
	};

	private static Lap createLap(Double avgHeartRate, Double avgSpeed, Integer maxHeartRate, Double maxSpeed,
			Double totalTimeSeconds, Double distanceMeters, int index, long startTimeMilliseconds) {
		Lap lap = new Lap();
		lap.setAverageHearRate(avgHeartRate);
		lap.setAverageSpeed(avgSpeed);
		lap.setMaximunHeartRate(maxHeartRate);
		lap.setMaximunSpeed(maxSpeed);
		lap.setTotalTimeSeconds(totalTimeSeconds);
		lap.setDistanceMeters(distanceMeters);
		lap.setIndex(index);
		lap.setStartTime(new Date(startTimeMilliseconds));
		return lap;
	}

	private static Activity createActivityByXmlType(String xmlType, String id) {
		Activity activity = new Activity();
		activity.setDevice("Garmin Connect");
		activity.setName("Untitled");
		activity.setDate(new Date(1311586432000L));
		activity.setSourceXmlType(xmlType);
		activity.setId(id);
		return activity;
	}

}
