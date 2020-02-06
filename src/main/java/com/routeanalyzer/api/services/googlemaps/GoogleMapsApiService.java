package com.routeanalyzer.api.services.googlemaps;

import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.ElevationService;
import com.routeanalyzer.api.services.googlemaps.model.GoggleMapsAPIResponse;
import com.routeanalyzer.api.services.googlemaps.model.GoogleMapsAPIResult;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.not;
import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static com.routeanalyzer.api.common.Constants.POSITIONS_DELIMITER;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapsApiService implements ElevationService {

	private static final String PARAMS = "locations={positions}&key={apiKey}";
	private static final String RESULT_STATUS_MAP_KEY = "status";

	private final GoogleMapsApiProperties properties;
	private final RestTemplate restTemplate;

	private String getUrl(String positions) {
		return UriComponentsBuilder.newInstance()
				.scheme(properties.getElevationProtocol())
				.host(properties.getElevationHost())
				.path(properties.getElevationEndpoint())
				.query(PARAMS)
				.buildAndExpand(positions, properties.getApiKey())
				.toUriString();
	}

	@Override
	public Map<String, String> getAltitude(String positions) {
		return ofNullable(positions)
				.filter(StringUtils::isNotEmpty)
				.flatMap(__ -> Try.of(() -> restTemplate.getForObject(getUrl(positions), GoggleMapsAPIResponse.class))
						.onFailure(err -> log.error("Error getting the altitude of positions: {}", positions, err))
						.toJavaOptional())
				.map(this::toMapResult)
				.orElse(emptyMap());
	}

	private Map<String, String> toMapResult(GoggleMapsAPIResponse gmResponse) {
		return of(HttpStatus.OK)
				.map(HttpStatus::getReasonPhrase)
				.filter(gmResponse.getStatus()::equalsIgnoreCase)
				.map(__ -> gmResponse.getResults()
						.stream()
						.collect(toMap(this::calculateMapKey, this::calculateMapValue)))
				.map(resultMap -> addStatusToResponse(resultMap, gmResponse))
				.orElseGet(() -> singletonMap(RESULT_STATUS_MAP_KEY, gmResponse.getStatus()));
	}

	private String calculateMapKey(GoogleMapsAPIResult gmResult) {
		return of(gmResult)
				.map(GoogleMapsAPIResult::getLocation)
				.filter(gmLocation -> nonNull(gmLocation.getLat()))
				.filter(gmLocation -> nonNull(gmLocation.getLng()))
				.map(gmLocation -> joinByComma(gmLocation.getLat(), gmLocation.getLng()))
				.orElse(null);
	}

	private String calculateMapValue(GoogleMapsAPIResult gmResult) {
		return String.valueOf(gmResult.getElevation());
	}

	private Map<String, String> addStatusToResponse(Map<String, String> resultMap, GoggleMapsAPIResponse gmResponse) {
		resultMap.put(RESULT_STATUS_MAP_KEY, gmResponse.getStatus());
		return resultMap;
	}

	public String createPositionsRequest(List<TrackPoint> trackPointList) {
		Predicate<TrackPoint> isTrackValid = trackPoint -> isNull(trackPoint.getAltitudeMeters())
				&& nonNull(trackPoint.getPosition());
		return ofNullable(trackPointList)
				.filter(not(List::isEmpty))
				.map(trackPoints -> trackPoints
						.stream()
						.filter(isTrackValid)
						.map(this::getCoordinatesCode)
						.collect(joining(POSITIONS_DELIMITER)))
				.orElse(null);
	}

	public String getCoordinatesCode(TrackPoint trackPoint) {
		return ofNullable(trackPoint)
				.map(TrackPoint::getPosition)
				.filter(position -> nonNull(position.getLatitudeDegrees()))
				.filter(position -> nonNull(position.getLongitudeDegrees()))
				.map(position -> joinByComma(position.getLatitudeDegrees(), position.getLongitudeDegrees()))
				.orElse(null);
	}

	private String joinByComma(Object firstParam, Object secondParam) {
		return format("%s%s%s", firstParam, COMMA_DELIMITER, secondParam);
	}

}
