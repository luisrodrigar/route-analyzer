package com.routeanalyzer.api.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static com.routeanalyzer.api.common.Encrypter.decrypt;
import static java.util.Optional.ofNullable;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AWSConfigurationProperties.class)
public class AWSConfiguration {

    private final AWSConfigurationProperties properties;

    @Bean
    public AmazonS3 s3client() {
        return createCredentials(properties.getDecodeAccessKeyId(), properties.getDecodeSecretAccessKey())
                .map(this::createAmazonS3HTTPProtocol)
                .orElse(null);
    }

    private Optional<BasicAWSCredentials> createCredentials(final String accessKeyId, final String secretAccessKey) {
        return ofNullable(accessKeyId)
                .filter(StringUtils::isNotEmpty)
                .flatMap(__ -> ofNullable(secretAccessKey)
                        .filter(StringUtils::isNotEmpty)
                        .map(___ -> new BasicAWSCredentials(accessKeyId, secretAccessKey)));
    }

    private AmazonS3 createAmazonS3HTTPProtocol(final BasicAWSCredentials awsCredentials) {
        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTP);
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(properties.getS3Region()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withClientConfiguration(config)
                .build();
    }

}
