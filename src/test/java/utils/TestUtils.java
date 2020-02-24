package utils;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mongodb.Function;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.facade.impl.ActivityFacadeImplTest;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.xml.gpx11.ExtensionsType;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.MetadataType;
import com.routeanalyzer.api.xml.gpx11.TrkType;
import com.routeanalyzer.api.xml.gpx11.TrksegType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import com.routeanalyzer.api.xml.tcx.ActivityLapT;
import com.routeanalyzer.api.xml.tcx.ActivityListT;
import com.routeanalyzer.api.xml.tcx.ActivityT;
import com.routeanalyzer.api.xml.tcx.ExtensionsT;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import com.routeanalyzer.api.xml.tcx.TrackT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityTrackpointExtensionT;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

import static java.nio.file.Files.newInputStream;

@Slf4j
@UtilityClass
public class TestUtils {

	public static final String GPX_ID_XML 			= "5add80014e237c10148aa6b1";
	public static final String TCX_ID_XML 			= "bac08c0b4e2a7910148aa5c2";
	public static final String ACTIVITY_GPX_ID 		= "5ace8cd14c147400048aa6b0";
	public static final String ACTIVITY_TCX_ID 		= "5ace8caf4c147400048aa6af";
	public static final String ACTIVITY_TCX_1_ID 	= "9aab30a74e13840004822bcb";
	public static final String ACTIVITY_TCX_2_ID 	= "5f1b82a74e138400048bb60a";
	public static final String ACTIVITY_TCX_3_ID 	= "1b1b82a74e13840004822c1a";
	public static final String ACTIVITY_TCX_4_ID 	= "000b82a74e1384000481b10a";
	public static final String ACTIVITY_TCX_5_ID 	= "2b1b82a74aab840004822c1e";
	public static final String ACTIVITY_TCX_6_ID	= "cacb82a74aa1230004822c6a";
	public static final String NOT_EXIST_1_ID 		= "aaaccc111222000333555fff";
	public static final String NOT_EXIST_2_ID 		= "000ccc111222aaa333555bbb";

	public static Function<Path, S3ObjectInputStream> toS3ObjectInputStream = path -> Try.of(() ->
			new S3ObjectInputStream(newInputStream(path), null)).getOrNull();

	public static byte[] getFileBytes(Resource resource) {
		Function<Path, Try<byte[]>> toByteArray = path -> Try.of(() -> Files.readAllBytes(path));
		return Try.of(() -> resource.getFile().toPath()).flatMap(toByteArray::apply).getOrNull();
	}

	public static byte[] getFileBytes(String resourcePath)  {
		return Try.of(() -> IOUtils.toByteArray(getStreamResource(resourcePath)))
				.onFailure(err -> log.error("Error happenend trying to get the resource string", err))
				.getOrNull();
	}

	public static Activity toActivity(String resourcePath)  {
		return Try.of(() -> IOUtils.toString(getStreamResource(resourcePath), StandardCharsets.UTF_8))
				.onFailure(err -> log.error("Error happenend trying to get the resource string", err))
				.flatMap(jsonString -> JsonUtils.fromJson(jsonString, Activity.class))
				.getOrNull();
	}

	public static InputStream getStreamResource(final String resourcePath) {
		return Optional.ofNullable(resourcePath)
				.map(ActivityFacadeImplTest.class.getClassLoader()::getResourceAsStream)
				.orElseThrow(() -> new IllegalArgumentException("Error with the path parameter"));
	}

	public static GpxType toGpxRootModel(final String resourcePath)  {
		return Try.of(() -> IOUtils.toString(getStreamResource(resourcePath), StandardCharsets.UTF_8))
				.onFailure(err -> log.error("It could not be possible to get to json string"))
				.flatMap(jsonStr -> JsonUtils.fromJson(jsonStr, GpxType.class))
				.getOrNull();
	}

	public static TrainingCenterDatabaseT toTcxRootModel(final String resourcePath)  {
		return Try.of(() -> IOUtils.toString(getStreamResource(resourcePath), StandardCharsets.UTF_8))
				.onFailure(err -> log.error("It could not be possible to get to json string"))
				.flatMap(jsonStr -> JsonUtils.fromJson(jsonStr, TrainingCenterDatabaseT.class))
				.getOrNull();
	}

	public static Activity toActivity(Resource resource)  {
		return Try.of(() -> resource.getURL())
				.onFailure(err -> log.error("It could not be possible to get the url"))
				.flatMap(urlTcx -> Try.of(() -> Resources.toString(urlTcx, Charsets.UTF_8))
						.onFailure(err -> log.error("It could not be possible to get to json string"))
						.flatMap(jsonStr -> JsonUtils.fromJson(jsonStr, Activity.class)))
				.getOrNull();
	}

	public static GpxType toGpxRootModel(Resource resource)  {
		return Try.of(() -> resource.getURL())
				.onFailure(err -> log.error("It could not be possible to get the url"))
				.flatMap(urlTcx -> Try.of(() -> Resources.toString(urlTcx, Charsets.UTF_8))
						.onFailure(err -> log.error("It could not be possible to get to json string"))
						.flatMap(jsonStr -> JsonUtils.fromJson(jsonStr, GpxType.class)))
				.getOrNull();
	}


	public static GpxType createValidGpxType() {
		GpxType gpxType = new GpxType();
		gpxType.setMetadata(createMetadata());
		gpxType.setCreator("Garmin Connect");
		gpxType.addTrk(createTrkType());
		return gpxType;
	}

	private static MetadataType createMetadata() {
		MetadataType metadataType = new MetadataType();
		LocalDateTime localDateTime = LocalDateTime.of(2018, 2, 27, 13, 16, 13);
		metadataType.setTime(getXmlGregorianCalendar(ZonedDateTime.of(localDateTime, ZoneOffset.UTC)));
		return metadataType;
	}

	private static TrkType createTrkType() {
		TrkType trkType = new TrkType();
		trkType.addTrkseg(createTrksegType1());
		trkType.addTrkseg(createTrksegType2());
		trkType.addTrkseg(createTrksegType3());
		return trkType;
	}

	private static TrksegType createTrksegType1() {
		TrksegType trksegType = new TrksegType();
		LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 16, 13);
		LocalDateTime time2 = LocalDateTime.of(2018, 02, 27, 13, 16, 18);
		LocalDateTime time3 = LocalDateTime.of(2018, 02, 27, 13, 16, 20);
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6131970"), new BigDecimal("-6.5732170"), new BigDecimal(
				"557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132170"), new BigDecimal("-6.5733730"), new BigDecimal(
				"557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time2, ZoneOffset.UTC))));
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5734430"), new BigDecimal(
				"557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time3, ZoneOffset.UTC))));
		return trksegType;
	}

	private static TrksegType createTrksegType2() {
		TrksegType trksegType = new TrksegType();
		LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 16, 30);
		LocalDateTime time2 = LocalDateTime.of(2018, 02, 27, 13, 16, 33);
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250"), new BigDecimal(
				"557.3"), new Short("106"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5739120"), new BigDecimal(
				"557.3"), new Short("109"), getXmlGregorianCalendar(ZonedDateTime.of(time2, ZoneOffset.UTC))));
		return trksegType;
	}

	private static TrksegType createTrksegType3() {
		TrksegType trksegType = new TrksegType();
		LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 17, 30);
		trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250"), new BigDecimal(
				"557.3"), new Short("120"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
		return trksegType;
	}

	private static WptType createTrkpt(final BigDecimal lat, final BigDecimal lon, final BigDecimal elevation,
								final Short heartRate, final XMLGregorianCalendar time) {
		WptType trkpt = new WptType();
		trkpt.setTime(time);
		trkpt.setLat(lat);
		trkpt.setLon(lon);
		trkpt.setEle(elevation);
		trkpt.setExtensions(createExtensionType(heartRate));
		return trkpt;
	}

	private static ExtensionsType createExtensionType(final Short heartRate) {
		ExtensionsType extensionsType = new ExtensionsType();
		extensionsType.addAny(createTrackPointExtensionT(heartRate));
		return extensionsType;
	}

	private static TrackPointExtensionT createTrackPointExtensionT(final Short heartRate) {
		TrackPointExtensionT trackPointExtensionT = new TrackPointExtensionT();
		trackPointExtensionT.setHr(heartRate);
		return trackPointExtensionT;
	}

	private static XMLGregorianCalendar getXmlGregorianCalendar(final ZonedDateTime zonedDateTime) {
		GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
		return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
				.onFailure(err -> log.error("Error trying to create xml gregorian calendar"))
				.getOrNull();
	}

	public static TrainingCenterDatabaseT createValidTrainingCenterType() {
		TrainingCenterDatabaseT trainingCenterDatabaseT = new TrainingCenterDatabaseT();
		trainingCenterDatabaseT.setActivities(createTcxActivities());
		return trainingCenterDatabaseT;
	}

	private static ActivityListT createTcxActivities() {
		ActivityListT activityListT = new ActivityListT();
		activityListT.addActivity(createTcxActivity());
		return activityListT;
	}

	private static ActivityT createTcxActivity() {
		ActivityT activityT = new ActivityT();
		LocalDateTime localDateTime = LocalDateTime.of(2011, 7, 25, 9, 33, 52);
		activityT.setId(getXmlGregorianCalendar(ZonedDateTime.of(localDateTime, ZoneOffset.UTC)));
		LocalDateTime date1 = LocalDateTime.of(2018, 2, 27, 13, 16, 13);
		LocalDateTime date2 = LocalDateTime.of(2018, 2, 27, 13, 16, 40);
		activityT.addLap(createTcxLap(date1, 16.0, 43.313081753899034, 6.143949288527114,
				0, "97", "103", 2.6801723874025747, createTrackPoints1()));
		activityT.addLap(createTcxLap(date2, 1.0, 0.0, 6.717731656189975,
				0, "120", "120", 6.717731656189975, createTrackPoints2()));
		return activityT;
	}

	private static TrackT createTrackPoints1() {
		TrackpointT trackpointT1 = new TrackpointT();
		trackpointT1.setPosition(createPositionT(42.613197, -6.573217));
		LocalDateTime t1 = LocalDateTime.of(2018, 2, 27, 13, 16, 13);
		trackpointT1.setTime(getXmlGregorianCalendar(ZonedDateTime.of(t1, ZoneOffset.UTC)));
		trackpointT1.setAltitudeMeters(557.3);
		trackpointT1.setDistanceMeters(0.0);
		trackpointT1.setHeartRateBpm(createTcxHeartRate("96"));
		trackpointT1.setExtensions(createTcxTrackSpeedExtension(0.0));
		TrackpointT trackpointT2 = new TrackpointT();
		trackpointT2.setPosition(createPositionT(42.613217, -6.573373));
		LocalDateTime t2 = LocalDateTime.of(2018, 2, 27, 13, 16, 18);
		trackpointT2.setTime(getXmlGregorianCalendar(ZonedDateTime.of(t2, ZoneOffset.UTC)));
		trackpointT2.setAltitudeMeters(557.3);
		trackpointT2.setDistanceMeters(12.972326303345616);
		trackpointT2.setHeartRateBpm(createTcxHeartRate("96"));
		trackpointT2.setExtensions(createTcxTrackSpeedExtension(2.594465260669123));
		TrackpointT trackpointT3 = new TrackpointT();
		trackpointT3.setPosition(createPositionT(42.613212, -6.573443));
		LocalDateTime t3 = LocalDateTime.of(2018, 2, 27, 13, 16, 20);
		trackpointT3.setTime(getXmlGregorianCalendar(ZonedDateTime.of(t3, ZoneOffset.UTC)));
		trackpointT3.setAltitudeMeters(557.3);
		trackpointT3.setDistanceMeters(18.734670396302985);
		trackpointT3.setHeartRateBpm(createTcxHeartRate("96"));
		trackpointT3.setExtensions(createTcxTrackSpeedExtension(2.8811720464786834));
		TrackT trackT = new TrackT();
		trackT.addTrackpoint(trackpointT1);
		trackT.addTrackpoint(trackpointT2);
		trackT.addTrackpoint(trackpointT3);
		return trackT;
	}

	private static ExtensionsT createTcxTrackSpeedExtension(double speed) {
		ExtensionsT extensionsT = new ExtensionsT();
		ActivityTrackpointExtensionT extensionT = new ActivityTrackpointExtensionT();
		extensionT.setSpeed(speed);
		extensionsT.addAny(extensionT);
		return extensionsT;
	}

	private static PositionT createPositionT(double lat, double lng) {
		PositionT positionT = new PositionT();
		positionT.setLatitudeDegrees(lat);
		positionT.setLongitudeDegrees(lng);
		return positionT;
	}

	private static TrackT createTrackPoints2() {
		TrackpointT trackpointT1 = new TrackpointT();
		trackpointT1.setPosition(createPositionT(42.613212, -6.573825));
		LocalDateTime t1 = LocalDateTime.of(2018, 2, 27, 13, 17, 30);
		trackpointT1.setTime(getXmlGregorianCalendar(ZonedDateTime.of(t1, ZoneOffset.UTC)));
		trackpointT1.setAltitudeMeters(557.3);
		trackpointT1.setDistanceMeters(106.89949587498064);
		trackpointT1.setHeartRateBpm(createTcxHeartRate("120"));
		trackpointT1.setExtensions(createTcxTrackSpeedExtension(6.717731656189976));
		TrackT trackT = new TrackT();
		trackT.addTrackpoint(trackpointT1);
		return trackT;
	}

	private static ActivityLapT createTcxLap(LocalDateTime startTime, double totalTimeSeconds, double distanceMeters, double maxSpeed, int calories,
									  String avgHeartRate, String maxHeartRate, Double avgSpeed, TrackT tracks) {
		ActivityLapT activityLapT = new ActivityLapT();
		activityLapT.setStartTime(getXmlGregorianCalendar(ZonedDateTime.of(startTime, ZoneOffset.UTC)));
		activityLapT.setTotalTimeSeconds(totalTimeSeconds);
		activityLapT.setDistanceMeters(distanceMeters);
		activityLapT.setMaximumSpeed(maxSpeed);
		activityLapT.setCalories(calories);
		activityLapT.setAverageHeartRateBpm(createTcxHeartRate(avgHeartRate));
		activityLapT.setMaximumHeartRateBpm(createTcxHeartRate(maxHeartRate));
		activityLapT.setExtensions(createExtensionLapAvgSpeed(avgSpeed));
		activityLapT.addTrack(tracks);
		return activityLapT;
	}

	private static HeartRateInBeatsPerMinuteT createTcxHeartRate(String value) {
		HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = new HeartRateInBeatsPerMinuteT();
		heartRateInBeatsPerMinuteT.setValue(Short.parseShort(value));
		return heartRateInBeatsPerMinuteT;
	}

	private static ExtensionsT createExtensionLapAvgSpeed(Double value) {
		ExtensionsT extensionsT = new ExtensionsT();
		ActivityLapExtensionT activityLapExtensionT = new ActivityLapExtensionT();
		activityLapExtensionT.setAvgSpeed(value);
		extensionsT.addAny(activityLapExtensionT);
		return extensionsT;
	}

}
