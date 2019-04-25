package com.routeanalyzer.api.services.googlemaps;

import com.google.common.collect.Maps;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.ElevationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static com.routeanalyzer.api.common.Constants.POSITIONS_DELIMITER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class GoogleMapsServiceImpl implements ElevationService {
	
	@Value("${google.maps.key}")
	private String googleMapsKey;
	@Value("${google.maps.elevation.url}")
	private String urlPathElevation;

	public Map<String, String> getAltitude(String positions) {
		String urlLocations = "locations=" + positions;
		String url = urlPathElevation + "json?" + urlLocations + "&key=" + googleMapsKey;
		RestTemplate restTemplate = new RestTemplate();
		GMResponse response = restTemplate.getForObject(url, GMResponse.class);
		Map<String, String> result = Maps.newHashMap();
		result.put("status", response.getStatus());
		if("OK".equalsIgnoreCase(response.getStatus())){
			response.getResults().forEach(eachResult -> {
				String key = eachResult.getLocation().getLat() + COMMA_DELIMITER
						+ eachResult.getLocation().getLng();
				result.put(key, eachResult.getElevation().toString());
			});
		}
		return result;
	}

	public String createPositionsRequest(List<TrackPoint> trackPointList) {
		Predicate<TrackPoint> isTrackValid = trackPoint -> isNull(trackPoint.getAltitudeMeters())
				&& nonNull(trackPoint.getPosition());
		return trackPointList.stream()
				.filter(isTrackValid)
				.map(trackPoint ->
						trackPoint.getPosition().getLatitudeDegrees() + COMMA_DELIMITER +
								trackPoint.getPosition().getLongitudeDegrees())
				.collect(Collectors.joining(POSITIONS_DELIMITER));
	}

	public String getCoordinatesCode(TrackPoint trackPoint) {
		return trackPoint.getPosition().getLatitudeDegrees() + COMMA_DELIMITER
				+ trackPoint.getPosition().getLongitudeDegrees();
	}

}
