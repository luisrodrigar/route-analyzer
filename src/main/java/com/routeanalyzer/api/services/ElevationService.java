package com.routeanalyzer.api.services;

import java.util.Map;

public interface ElevationService {
	
	Map<String, String> getAltitude(String positions);
	
}
