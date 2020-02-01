package com.routeanalyzer.api.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.services.googlemaps.GoogleMapsAPIProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Optional;

import static com.routeanalyzer.api.common.Encrypter.decrypt;
import static java.util.Optional.ofNullable;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
@EnableConfigurationProperties({AWSConfigurationProperties.class, GoogleMapsAPIProperties.class})
@EnableMongoRepositories(basePackageClasses=ActivityMongoRepository.class)
public class CommonConfig {

    private final AWSConfigurationProperties properties;

    @Bean
    public AmazonS3 s3client() {
        return createCredentials(properties.getAccessKeyId(), properties.getSecretAccessKey())
                .map(this::createAmazonS3HTTPProtocol)
                .orElse(null);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private Optional<BasicAWSCredentials> createCredentials(String encryptedAwsId, String encryptedAwsKey) {
        return ofNullable(encryptedAwsId)
                .filter(StringUtils::isNotEmpty)
                .flatMap(__ -> ofNullable(encryptedAwsKey)
                        .filter(StringUtils::isNotEmpty)
                        .map(___ -> new BasicAWSCredentials(decrypt(encryptedAwsId), decrypt(encryptedAwsKey))));
    }

    private AmazonS3 createAmazonS3HTTPProtocol(BasicAWSCredentials awsCredentials) {
        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTP);
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(properties.getS3Region()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withClientConfiguration(config)
                .build();
    }

}
