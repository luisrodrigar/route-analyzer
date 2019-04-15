package com.routeanalyzer.api.services.googlemaps;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GMResponse {
	private String status;
	private List<GMResult> results;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<GMResult> getResults() {
		return results;
	}

	public void setResults(List<GMResult> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "GMResponse [status=" + status + ", results=" + results + "]";
	}
}
