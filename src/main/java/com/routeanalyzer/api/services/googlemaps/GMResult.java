package com.routeanalyzer.api.services.googlemaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GMResult {
	private Double elevation;
	private GMPosition location;
	private Double resolution;
	public Double getElevation() {
		return elevation;
	}
	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}
	public GMPosition getLocation() {
		return location;
	}
	public void setLocation(GMPosition location) {
		this.location = location;
	}
	public Double getResolution() {
		return resolution;
	}
	public void setResolution(Double resolution) {
		this.resolution = resolution;
	}
	@Override
	public String toString() {
		return "GMResult [elevation=" + elevation + ", location=" + location + ", resolution=" + resolution + "]";
	}
	
}
