package com.routeanalyzer.api.services.googlemaps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoggleMapsAPIResponse {
	private String status;
	@Builder.Default
	private List<GoogleMapsAPIResult> results = Lists.newArrayList();
}
