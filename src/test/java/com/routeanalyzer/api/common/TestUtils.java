package com.routeanalyzer.api.common;

import com.mongodb.Function;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Supplier;

@UtilityClass
public class TestUtils {

	public static final String ACTIVITY_GPX_ID = "5ace8cd14c147400048aa6b0";
	public static final String ACTIVITY_TCX_ID = "5ace8caf4c147400048aa6af";

	public static Supplier<Activity> createUnknownActivity = () -> Activity.builder().build();

	public static byte[] getFileBytes(Resource resource) {
		Function<Path, Try<byte[]>> toByteArray = path -> Try.of(() -> Files.readAllBytes(path));
		return Try.of(() -> resource.getFile().toPath()).flatMap(toByteArray::apply).getOrNull();
	}

	public static Function<Path, BufferedReader> toBufferedReader = path -> Try.of(() ->
			Files.newBufferedReader(path, StandardCharsets.UTF_8)).getOrNull();

	public static Supplier<Activity> createGPXActivity = () -> createActivityByXmlType("gpx", ACTIVITY_GPX_ID);

	public static LocalDateTime toLocalDateTime(long timeMillis) {
		return Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static RuntimeException toRuntimeException(Exception exception) {
		return new RuntimeException(exception);
	}

	public static Supplier<Activity> createTCXActivity = () -> {
		Activity activity = createActivityByXmlType("tcx", ACTIVITY_TCX_ID);
		Lap lap1 = createLap(98.6231732776618, 4.112047870706494, 103, 6.143949288527114, 16.0, 43.34577092311236, 1,
				1519737373000L);
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737373000L))
				.index(1)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6131970"))
						.longitudeDegrees(new BigDecimal("-6.5732170")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("0"))
				.speed(new BigDecimal("0"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737376000L))
				.index(2)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5733020")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("7.16095400022874173373566009104251861572265625"))
				.speed(new BigDecimal("2.386984666742913763215483413659967482089996337890625"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737378000L))
				.index(3)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132170"))
						.longitudeDegrees(new BigDecimal("-6.5733730")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("13.005015472558941524994224891997873783111572265625"))
				.speed(new BigDecimal("2.9220307361650998956292824004776775836944580078125"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737380000L))
				.index(4)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5734430")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("18.76735956551630835065225255675613880157470703125"))
				.speed(new BigDecimal("2.8811720464786834128290138323791325092315673828125"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737383000L))
				.index(5)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5735170")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("24.829908844772916154397535137832164764404296875"))
				.speed(new BigDecimal("2.02084975975220260124842752702534198760986328125"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737384000L))
				.index(6)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5735920")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("30.973858133300030459622576017864048480987548828125"))
				.speed(new BigDecimal("6.143949288527114305225040880031883716583251953125"))
				.heartRateBpm(96)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737386000L))
				.index(7)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5736650")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("36.95465469202076036481230403296649456024169921875"))
				.speed(new BigDecimal("2.9903982793603649525948640075512230396270751953125"))
				.heartRateBpm(99)
				.build());
		lap1.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737389000L))
				.index(8)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5737430")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("43.34577092311236157229359378106892108917236328125"))
				.speed(new BigDecimal("2.130372077030533883856833199388347566127777099609375"))
				.heartRateBpm(103)
				.build());
		activity.addLap(lap1);
		Lap lap2 = createLap(112.2, 4.112047870706494, 116, 7.053560612105727, 8.0, 28.934607828341, 2, 1519737390000L);
		
		lap2.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737390000L))
				.index(1)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5738250")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("50.06350257930233738079550676047801971435546875"))
				.speed(new BigDecimal("6.71773165618997580850191297940909862518310546875"))
				.heartRateBpm(106)
				.build());
		lap2.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737393000L))
				.index(2)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5739120")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("57.191584795088687087627477012574672698974609375"))
				.speed(new BigDecimal("2.376027405262116420914253467344678938388824462890625"))
				.heartRateBpm(109)
				.build());
		lap2.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737395000L))
				.index(3)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132170"))
						.longitudeDegrees(new BigDecimal("-6.5739970")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("64.178236716566146924378699623048305511474609375"))
				.speed(new BigDecimal("3.49332596073872991837561130523681640625"))
				.heartRateBpm(115)
				.build());
		lap2.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737396000L))
				.index(4)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132200"))
						.longitudeDegrees(new BigDecimal("-6.5740830")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("71.2317973286718739700518199242651462554931640625"))
				.speed(new BigDecimal("7.0535606121057270456731203012168407440185546875"))
				.heartRateBpm(115)
				.build());
		lap2.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737398000L))
				.index(5)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132350"))
						.longitudeDegrees(new BigDecimal("-6.5741700")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("78.5524299205609253249349421821534633636474609375"))
				.speed(new BigDecimal("3.6603162959445256774415611289441585540771484375"))
				.heartRateBpm(116)
				.build());
		activity.addLap(lap2);
		Lap lap3 = createLap(120.0, 6.717731656189975, 120, 6.717731656189975, 1.0, 0.0, 3, 1519737400000L);
		lap3.addTrack(TrackPoint.builder()
				.date(toLocalDateTime(1519737450000L))
				.index(1)
				.position(Position.builder()
						.latitudeDegrees(new BigDecimal("42.6132120"))
						.longitudeDegrees(new BigDecimal("-6.5738250")).build())
				.altitudeMeters(new BigDecimal("557.3"))
				.distanceMeters(new BigDecimal("0.0"))
				.speed(new BigDecimal("6.71773165618997580850191297940909862518310546875"))
				.heartRateBpm(120)
				.build());
		activity.addLap(lap3);
		return activity;
	};

	private static Lap createLap(Double avgHeartRate, Double avgSpeed, Integer maxHeartRate, Double maxSpeed,
			Double totalTimeSeconds, Double distanceMeters, int index, long startTimeMilliseconds) {
		return Lap.builder()
				.averageHearRate(avgHeartRate)
				.averageSpeed(avgSpeed)
				.maximumHeartRate(maxHeartRate)
				.maximumSpeed(maxSpeed)
				.totalTimeSeconds(totalTimeSeconds)
				.distanceMeters(distanceMeters)
				.startTime(toLocalDateTime(startTimeMilliseconds))
				.index(index)
				.build();
	}

	private static Activity createActivityByXmlType(String xmlType, String id) {
		return Activity.builder()
				.device("Garmin Connect")
				.name("Untitled")
				.date(toLocalDateTime(1311586432000L))
				.sourceXmlType(xmlType)
				.id(id)
				.build();
	}

}
