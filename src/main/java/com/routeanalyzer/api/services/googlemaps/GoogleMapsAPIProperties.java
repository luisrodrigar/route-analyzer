package com.routeanalyzer.api.services.googlemaps;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Value
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "google-maps-api")
public class GoogleMapsAPIProperties {
    private final String elevationUrlEndpoint;
    private final String apiKey;
}
