package com.routeanalyzer.services.googlemaps;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.routeanalyzer.services.ElevationService;
import com.routeanalyzer.services.googlemaps.GMResponse;

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
		Map<String, String> result = new HashMap<String, String>();
		result.put("status", response.getStatus());
		if("OK".equalsIgnoreCase(response.getStatus())){
			response.getResults().forEach(eachResult -> {
				String key = eachResult.getLocation().getLat() + "," + eachResult.getLocation().getLng();
				result.put(key, eachResult.getElevation().toString());
			});
		}
		return result;
	}

}
