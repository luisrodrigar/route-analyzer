package com.routeanalyzer.api.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.Optional;
import java.util.Random;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test-as3")
@ContextConfiguration(classes = {
        OriginalRouteAS3ServiceImpl.class,
        OriginalRouteAS3ServiceImplTest.OriginalRouteS3ContextConfiguration.class
})
@TestPropertySource(properties = {
        "jsa.s3.bucket=route-analyzer-bucket-test"
})
public class OriginalRouteAS3ServiceImplTest {

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    @ClassRule
    public static LocalStackContainer localStack = new LocalStackContainer().withServices(S3);

    @Autowired
    private OriginalRouteAS3ServiceImpl originalRouteAS3Service;

    @SpyBean
    private AmazonS3 amazonS3;

    private String fileName;
    private byte[] bytes;

    @Before
    public void setUp() {
        fileName = "hello1.txt";
        Random random = new Random();
        random.setSeed(1);
        bytes = new byte[16];
        random.nextBytes(bytes);
    }

    @Test
    public void s3Test(){
        getFileNotExistsTest();
        uploadFile();
        getFileExistsTest();
    }

    private void getFileNotExistsTest() {
        // Given the global var fileName
        // When
        Optional<S3ObjectInputStream> bufferedReader = originalRouteAS3Service.getFile(fileName);

        // Then
        assertThat(bufferedReader).isEmpty();
    }

    private void uploadFile() {
        // Given the global vars: fileName and bytes
        // When
        originalRouteAS3Service.uploadFile(bytes, fileName);

        // Then
        verify(amazonS3).putObject(any(PutObjectRequest.class));
        assertThat(amazonS3.listObjectsV2(bucketName).getKeyCount()).isEqualTo(1);

        byte[] actualResult = Try.of(() -> toByteArray(amazonS3.getObject(bucketName, fileName).getObjectContent()))
                .getOrElse(() -> null);
        assertThat(bytes).isNotNull();
        assertThat(bytes).isEqualTo(actualResult);

    }

    public void getFileExistsTest() {
        // Given the global var fileName
        // When
        Optional<S3ObjectInputStream> bufferedReader = originalRouteAS3Service.getFile(fileName);

        // Then
        verify(amazonS3).getObject(eq(bucketName), eq(fileName));
        assertThat(bufferedReader).isNotEmpty();
        assertThat(Try.of(() -> toByteArray(bufferedReader.get())).getOrElse(() -> null))
                .isEqualTo(bytes);
    }

    @TestConfiguration
    @Profile("test-as3")
    static class OriginalRouteS3ContextConfiguration {
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
}