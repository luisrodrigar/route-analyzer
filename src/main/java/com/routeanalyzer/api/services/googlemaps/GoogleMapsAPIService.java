package com.routeanalyzer.api.services.googlemaps;

import com.google.common.collect.Maps;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.ElevationService;
import com.routeanalyzer.api.services.googlemaps.model.GoogleMapsAPIPosition;
import com.routeanalyzer.api.services.googlemaps.model.GoggleMapsAPIResponse;
import com.routeanalyzer.api.services.googlemaps.model.GoogleMapsAPIResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;
import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static com.routeanalyzer.api.common.Constants.POSITIONS_DELIMITER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class GoogleMapsAPIService implements ElevationService {
	
	private final GoogleMapsAPIProperties properties;
	private final RestTemplate restTemplate;

	public Map<String, String> getAltitude(String positions) {
		String urlLocations = "locations=" + positions;
		String url = properties.getElevationUrlEndpoint() + "json?" + urlLocations + "&key=" + properties.getApiKey();
		Function<GoogleMapsAPIResult, String> getLocationKey = result -> ofNullable(result)
				.map(GoogleMapsAPIResult::getLocation)
				.map(GoogleMapsAPIPosition::getLat)
				.flatMap(latitude -> of(result)
						.map(GoogleMapsAPIResult::getLocation)
						.map(GoogleMapsAPIPosition::getLng)
						.flatMap(longitude ->
								joinByComma(latitude.toString(), longitude.toString())))
				.orElse(null);
		Function<GoogleMapsAPIResult, String> getElevationValue = result -> result.getElevation().toString();
		Function<Map<String, String>, Function<String, Map<String, String>>> setStatusResponse =
				mapElevations -> status -> {
			mapElevations.put("status", status);
			return mapElevations;
		};
		Function<GoggleMapsAPIResponse, Supplier<Map<String, String>>> createNonOKResponseMap = response -> () ->
				setStatusResponse.apply(Maps.newHashMap())
				.apply(response.getStatus());
		return ofNullable(positions)
				.filter(StringUtils::isNotEmpty)
				.map(__ -> restTemplate.getForObject(url, GoggleMapsAPIResponse.class))
				.filter(Objects::nonNull)
				.map(response -> of(response)
						.map(GoggleMapsAPIResponse::getStatus)
						.filter("OK"::equalsIgnoreCase)
						.map(statusResult -> of(response)
								.map(GoggleMapsAPIResponse::getResults)
								.map(result -> result.stream()
										.collect(toMap(getLocationKey, getElevationValue)))
								.orElseGet(Collections::emptyMap))
						.map(setStatusResponse)
						.map(setterStatusResponse -> setterStatusResponse.apply(response.getStatus()))
						.orElseGet(createNonOKResponseMap.apply(response)))
				.orElseGet(Collections::emptyMap);
	}

	public String createPositionsRequest(List<TrackPoint> trackPointList) {
		Predicate<TrackPoint> isTrackValid = trackPoint -> isNull(trackPoint.getAltitudeMeters())
				&& nonNull(trackPoint.getPosition());
		return ofNullable(trackPointList)
				.filter(not(List::isEmpty))
				.map(List::stream)
				.map(trackPointStream -> trackPointStream
						.filter(isTrackValid)
						.map(this::getCoordinatesCode)
						.collect(Collectors.joining(POSITIONS_DELIMITER)))
				.orElse(null);
	}

	public String getCoordinatesCode(TrackPoint trackPoint) {
		return ofNullable(trackPoint)
				.map(TrackPoint::getPosition)
				.map(Position::getLatitudeDegrees)
				.map(BigDecimal::toString)
				.flatMap(latitude -> of(trackPoint.getPosition())
						.map(Position::getLongitudeDegrees)
						.map(BigDecimal::toString)
						.flatMap(longitude -> joinByComma(latitude, longitude)))
				.orElse(null);
	}

	private Optional<String> joinByComma(String first, String second) {
		return ofNullable(first)
				.filter(Objects::nonNull)
				.flatMap(firstParam -> ofNullable(second)
						.filter(Objects::nonNull)
						.map(secondParam -> firstParam + COMMA_DELIMITER + secondParam));
	}

}
