package com.routeanalyzer.api.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static com.routeanalyzer.api.common.Encrypter.decrypt;

@Value
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "route-analyzer")
public class MongoProperties {

    private final String encryptedMongoUri;
    private final String mongoDatabase;

    public String getMongoUri() {
        return decrypt(encryptedMongoUri);
    }
}
