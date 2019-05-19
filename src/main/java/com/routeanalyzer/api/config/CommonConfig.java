package com.routeanalyzer.api.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

import static com.routeanalyzer.api.common.Encrypter.decrypt;

@Configuration
@EnableMongoRepositories(basePackageClasses=ActivityMongoRepository.class)
public class CommonConfig {

    @Value("${jsa.aws.access_key_id}")
    private String encryptedAwsId;

    @Value("${jsa.aws.secret_access_key}")
    private String encryptedAwsKey;

    @Value("${jsa.s3.region}")
    private String region;

    @Bean
    public AmazonS3 s3client() {
        ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTP);
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(decrypt(encryptedAwsId), decrypt(encryptedAwsKey));
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withClientConfiguration(config)
                .build();

        return s3Client;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
