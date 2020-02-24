package com.routeanalyzer.api.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Value
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "route-analyzer")
public class MongoProperties {

    private final String mongoUri;
    private final String mongoDatabase;
}
