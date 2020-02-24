package com.routeanalyzer.api.services.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.routeanalyzer.api.config.AWSConfigurationProperties;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Random;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        AmazonS3Exception noSuchKey = new AmazonS3Exception("NoSuchKey");
        when(amazonS3.getObject(any(GetObjectRequest.class))).thenThrow(noSuchKey);
        Optional<S3ObjectInputStream> bufferedReader = originalActivityS3Service.getFile(fileName);

        // Then
        verify(amazonS3).getObject(any(GetObjectRequest.class));
        verify(properties).getS3Bucket();
        assertThat(bufferedReader).isEmpty();
    }

    @Test
    public void uploadFile() {
        // Given
        String contentMd5 = "CONTENT-MD5";
        PutObjectResult putObjectResult = new PutObjectResult();
        putObjectResult.setContentMd5(contentMd5);
        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);

        // When
        originalActivityS3Service.uploadFile(bytes, fileName);

        // Then
        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(properties).getS3Bucket();
    }

    @Test
    public void getFileExistsTest() {
        // When
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(bytes));
        when(amazonS3.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);

        // Given
        Optional<S3ObjectInputStream> bufferedReader = originalActivityS3Service.getFile(fileName);

        // Then
        verify(amazonS3).getObject(any(GetObjectRequest.class));
        assertThat(bufferedReader).isNotEmpty();
        assertThat(Try.of(() -> toByteArray(bufferedReader.get())).getOrNull())
                .isEqualTo(bytes);
    }
}
