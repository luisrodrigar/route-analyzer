package com.routeanalyzer.api.services.googlemaps;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static com.routeanalyzer.api.common.Encrypter.decrypt;

@Data
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "google-maps-api")
public class GoogleMapsApiProperties {
    private final String elevationHost;
    private final String elevationEndpoint;
    private final String elevationProtocol;
    private final String encryptedApiKey;

    public String getApiKey() {
        return decrypt(encryptedApiKey);
    }
}
