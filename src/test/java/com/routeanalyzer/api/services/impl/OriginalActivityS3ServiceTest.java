package com.routeanalyzer.api.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.config.AWSConfigurationProperties;
import com.routeanalyzer.api.services.aws.OriginalActivityS3Service;
import io.vavr.control.Try;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Random;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OriginalActivityS3ServiceTest {

    private static final String BUCKET_TEST_NAME = "route-analyzer-test-bucket";

    @Mock
    private static AmazonS3 amazonS3;
    @Mock
    private AWSConfigurationProperties properties;
    @InjectMocks
    private OriginalActivityS3Service originalActivityS3Service;

    private static String fileName;
    private static byte[] bytes;

    @Before
    public void mockProperties() {
        when(properties.getS3Bucket()).thenReturn(BUCKET_TEST_NAME);
    }

    @BeforeClass
    public static void setUp() {
        fileName = "hello1.txt";
        Random random = new Random();
        random.setSeed(1);
        bytes = new byte[16];
        random.nextBytes(bytes);
    }

    @Test
    public void getFileNotExistsTest() {
        // Given the global var fileName
        // When
        when(amazonS3.getObject(eq(BUCKET_TEST_NAME), eq(fileName))).thenReturn(null);
        Optional<S3ObjectInputStream> bufferedReader = originalActivityS3Service.getFile(fileName);

        // Then
        assertThat(bufferedReader).isEmpty();
    }

    @Test
    public void uploadFile() {
        // Given the global vars: fileName and bytes
        // When
        originalActivityS3Service.uploadFile(bytes, fileName);

        // Then
        verify(amazonS3).putObject(any(PutObjectRequest.class));
        assertThat(amazonS3.listObjectsV2(BUCKET_TEST_NAME).getKeyCount()).isEqualTo(1);

        byte[] actualResult = Try.of(() -> toByteArray(amazonS3.getObject(BUCKET_TEST_NAME, fileName).getObjectContent()))
                .getOrElse(() -> null);
        assertThat(bytes).isNotNull();
        assertThat(bytes).isEqualTo(actualResult);

    }

    @Test
    public void getFileExistsTest() {
        // Given the global var fileName
        // When
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent();
        when(amazonS3.getObject(eq(new GetObjectRequest(eq(BUCKET_TEST_NAME), eq(fileName))))).thenReturn(bytes);
        Optional<S3ObjectInputStream> bufferedReader = originalActivityS3Service.getFile(fileName);

        // Then
        verify(amazonS3).getObject(eq(BUCKET_TEST_NAME), eq(fileName));
        assertThat(bufferedReader).isNotEmpty();
        assertThat(Try.of(() -> toByteArray(bufferedReader.get())).getOrElse(() -> null))
                .isEqualTo(bytes);
    }
}