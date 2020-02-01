package com.routeanalyzer.api.it.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.ClassRule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@TestConfiguration
@Profile("test-as3")
public class S3AWSTestConfig {

    @ClassRule
    public static LocalStackContainer localStack = new LocalStackContainer().withServices(S3);

    @Bean(destroyMethod = "shutdown")
    public AmazonS3 amazonS3() {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(
                        localStack.getEndpointConfiguration(S3)
                )
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withPathStyleAccessEnabled(true)
                .disableChunkedEncoding()
                .enablePathStyleAccess()
                .build();
        amazonS3.createBucket("route-analyzer-bucket-test");
        return amazonS3;
    }
}
