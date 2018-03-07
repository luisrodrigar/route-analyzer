package com.routeanalyzer.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.routeanalyzer.services.googlemaps.GMResponse;

public class GoogleMapsService {
	private static final String KEY_GOOGLE_MAPS = "AIzaSyDEtc96UC9co31AFUNuNsPZ1xV4SYEMwfA";
	private static final String URL_PATH_ELEVATION = "https://maps.googleapis.com/maps/api/elevation/";
	private static final String QUERY_TYPE_OUTPUT = "json";
	private static final String QUERY_PARAM_LOCATION = "locations=";
	private static final String QUERY_PARAM_KEY = "key=";

	public static final Map<String, String> getAltitude(String positions) {
		String urlLocations = QUERY_PARAM_LOCATION + positions;
		String url = URL_PATH_ELEVATION + QUERY_TYPE_OUTPUT + "?" + urlLocations + "&" + QUERY_PARAM_KEY
				+ KEY_GOOGLE_MAPS;
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
