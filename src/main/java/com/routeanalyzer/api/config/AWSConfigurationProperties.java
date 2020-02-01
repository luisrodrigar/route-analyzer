package com.routeanalyzer.api.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aws")
public class AWSConfigurationProperties {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String s3Region;
    private final String s3Bucket;

}
