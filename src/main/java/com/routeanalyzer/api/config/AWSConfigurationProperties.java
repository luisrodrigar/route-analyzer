package com.routeanalyzer.api.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import static com.routeanalyzer.api.common.Encrypter.decrypt;

@Data
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aws")
public class AWSConfigurationProperties {

    private final String encryptedAccessKeyId;
    private final String encryptedSecretAccessKey;
    private final String s3Region;
    private final String s3Bucket;

    public String getDecodeAccessKeyId() {
        return decrypt(encryptedAccessKeyId);
    }

    public String getDecodeSecretAccessKey() {
        return decrypt(encryptedSecretAccessKey);
    }

}
