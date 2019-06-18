package com.routeanalyzer.api.common;

import com.routeanalyzer.api.controller.Response;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@UtilityClass
public class CommonUtils {

	public static <T> T toValueOrNull(String object, Function<String, T> convertTo) {
		return ofNullable(object)
				.map(convertTo)
				.orElse(null);
	}

	public static <T> T getFirstElement(List<T> list) {
		return ofNullable(list)
				.map(listParam -> listParam.get(0))
				.orElse(null);
	}

	// Model operations

	// Methods which generate a track point.

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return TrackPoint.builder()
				.date(dateTime)
				.index(index)
				.position(position)
				.altitudeMeters(toBigDecimal(alt))
				.distanceMeters(toBigDecimal(dist))
				.speed(toBigDecimal(speed))
				.heartRateBpm(heartRate)
				.build();
	}

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, Position position, Double alt, Double dist,
										  Double speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, position, toStringValue(alt), toStringValue(dist),
				toStringValue(speed), heartRate);
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar gregorianCalendar, int index, Position position,
										  String alt, String dist, String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(gregorianCalendar).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, Position position, String alt, String dist,
										  String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null),
				index, position, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(LocalDateTime dateTime, int index, String lat, String lng, String alt,
										  String dist, String speed, Integer heartRate) {
		return toTrackPoint(dateTime, index, toPosition(lat, lng), alt, dist, speed, heartRate);
	}
	
	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, Integer heartRate) {
		return toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null),
				index, lat, lng, alt, dist, speed, heartRate);
	}

	public static TrackPoint toTrackPoint(long timeMillis, int index, String lat, String lng, String alt, String dist,
											   String speed, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(heartRate)
				.map(heartRateXml ->
						toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
								Integer.valueOf(heartRateXml.getValue())))
				.orElse(toTrackPoint(DateUtils.toLocalDateTime(timeMillis).orElse(null), index, lat, lng, alt, dist, speed,
						null));
	}

	public static TrackPoint toTrackPoint(TrackpointT trackpointT, int indexTrackPoint) {
		return toTrackPoint(trackpointT.getTime(), indexTrackPoint, trackpointT.getPosition(),
				trackpointT.getAltitudeMeters(), trackpointT.getDistanceMeters(), trackpointT.getHeartRateBpm());
	}

	public static TrackPoint toTrackPoint(XMLGregorianCalendar xmlGregorianCalendar, int index, PositionT position,
										  Double alt, Double dist, HeartRateInBeatsPerMinuteT heartRate) {
		return ofNullable(position)
				.map(positionXml ->
						ofNullable(xmlGregorianCalendar)
								.flatMap(DateUtils::toLocalDateTime)
								.map(localDateTime ->
										ofNullable(heartRate).map(heartRateXml -> toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, Integer.valueOf(heartRateXml.getValue())))
											.orElse(toTrackPoint(localDateTime, index,
												toPosition(positionXml.getLatitudeDegrees(),
														positionXml.getLongitudeDegrees()),
												alt, dist, null, null))
							).orElse(null)
				).orElse(null);
	}

	// Methods which generate a position.

	public static Position toPosition(String latParam, String lngParam) {
		return toPosition(toBigDecimal(latParam), toBigDecimal(lngParam));
	}

	public static Optional<Position> toOptPosition(String latParam, String lngParam) {
		return toOptPosition(toBigDecimal(latParam), toBigDecimal(lngParam));
	}

	public static Position toPosition(Double lat, Double lng) {
		return toPosition(toStringValue(lat), toStringValue(lng));
	}

	public static Position toPosition(BigDecimal latParam, BigDecimal lngParam) {
		return ofNullable(latParam)
				.flatMap(latitude -> ofNullable(lngParam)
						.map(longitude -> new Position(latitude, longitude)))
				.orElse(null);
	}

	private static Optional<Position> toOptPosition(BigDecimal latParam, BigDecimal lngParam) {
		return ofNullable(latParam)
				.flatMap(latitude -> ofNullable(lngParam)
						.map(longitude -> new Position(latitude, longitude)));
	}


	// Response utils

	// Headers

	public static HttpHeaders toJsonHeaders(){
		MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
		values.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
		return toHeaders(values);
	}

	private static HttpHeaders toApplicationFileHeaders(String id, String fileType){
		MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
		values.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM.toString());
		values.add("Content-Disposition", "attachment;filename=" + id + "_" + fileType + ".xml");
		return toHeaders(values);
	}

	private static HttpHeaders toHeaders(MultiValueMap<String, String> values) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.addAll(values);
		return responseHeaders;
	}


	// Response Utils

	public static ResponseEntity<String> getFileExportResponse(String file, String id, String fileType) {
		return ResponseEntity.ok().headers(toApplicationFileHeaders(id, fileType)).body(file);
	}

	public static ResponseEntity<String> toBadRequestParams() {
		Response errorValue = Response.builder()
				.error(true)
				.description(BAD_REQUEST_MESSAGE)
				.errorMessage(null)
				.exception(null)
				.build();
		return badRequest().headers(toJsonHeaders()).body(JsonUtils.toJson(errorValue));
	}

	public static ResponseEntity<String> toBadRequestResponse(Object response) {
		return badRequest().headers(toJsonHeaders()).body(JsonUtils.toJson(response));
	}

	public static ResponseEntity<String> toOKMessageResponse(Object response) {
		return ok().headers(toJsonHeaders()).body(JsonUtils.toJson(response));
	}

	public static ResponseEntity<String> emptyResponse() {
		return status(HttpStatus.NOT_FOUND).body(JsonUtils.toJson(Collections.emptyList()));
	}

	// String utils

	public static List<String> splitStringByDelimiter(String string, String delimiter) {
		return ofNullable(string)
				.map(__ -> string.split(delimiter))
				.map(Stream::of)
				.map(streamSlices -> streamSlices
						.filter(StringUtils::isNotEmpty)
						.collect(toList()))
				.orElseGet(Collections::emptyList);
	}

	private static String toStringValue(Object value) {
		return ofNullable(value).map(String::valueOf).orElse(null);
	}


}
