package com.routeanalyzer.api.it;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.routeanalyzer.api.common.Constants.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static utils.TestUtils.*;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
public class FileRestControllerIntegrationTest extends IntegrationTest {

    private static final String CORUNA_XML_FILE = "coruna.gpx.xml";
    private static final String OVIEDO_XML_FILE = "oviedo.tcx.xml";

    @ClassRule
    public static LocalStackContainer localStackS3 = new LocalStackContainer().withServices(S3);

    @Rule
    public DockerComposeContainer mongoDbContainer =
            new DockerComposeContainer(new File(DOCKER_COMPOSE_MONGO_DB))
                    .withExposedService(MONGO_CONTAINER_NAME, MONGO_PORT);

    @Value("${aws.s3-bucket}")
    private String bucketName;

    @Value("classpath:controller/xml-input-fake-1.json")
    private Resource unknownResource;
    @Value("classpath:controller/coruna.gpx.xml")
    private Resource gpxXmlResource;
    @Value("classpath:controller/oviedo.tcx.xml")
    private Resource tcxXmlResource;

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private OriginalActivityRepository originalActivityRepository;
    @Autowired
    private ActivityMongoRepository activityMongoRepository;

    @Before
    public void setUp() {
        amazonS3.createBucket(bucketName);
    }

    @TestConfiguration
    public static class AWSTestConfiguration {

        @Bean
        @Primary
        public AmazonS3 s3client() {
            return AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(localStackS3.getEndpointConfiguration(S3))
                    .withCredentials(localStackS3.getDefaultCredentialsProvider())
                    .withPathStyleAccessEnabled(true)
                    .disableChunkedEncoding()
                    .enablePathStyleAccess()
                    .build();
        }
    }

    @Test
    public void uploadGPXFileTest() {
        // Given
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "controller", CORUNA_XML_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadGpxXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", SOURCE_GPX_XML)
                .build()
                .toUriString();

        // When
        ResponseEntity<String[]> result = testRestTemplate.exchange(uploadGpxXmlFile, POST, requestEntity, String[].class);

        List<String> ids = getIds(result);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(format("%s.%s", ids.get(0),
                SOURCE_GPX_XML));

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    @Test
    public void uploadTCXFileTest() {
        // Given
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "controller", OVIEDO_XML_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadTcxXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", SOURCE_TCX_XML)
                .build()
                .toUriString();

        // When
        ResponseEntity<String[]> result = testRestTemplate.exchange(uploadTcxXmlFile, POST, requestEntity, String[].class);

        List<String> ids = getIds(result);

        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(String.format("%s.%s",
                ids.get(0), SOURCE_TCX_XML));

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    private List<String> getIds(ResponseEntity<String[]> result) {
        return ofNullable(result)
                .map(ResponseEntity::getBody)
                .map(Stream::of)
                .map(stream -> stream.collect(toList()))
                .orElse(new ArrayList<>());
    }

    @Test
    public void uploadUnknownFileTest() {
        // Given
        String unknownXmlFile = "kml";
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "controller", OVIEDO_XML_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadUnknownXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", unknownXmlFile)
                .build()
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.exchange(uploadUnknownXmlFile, POST, requestEntity, String.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getGpxFileTest() {
        // Given

        // Upload xml file
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY,"true");
        Try.of(() -> IOUtils.toString(gpxXmlResource.getInputStream(), UTF_8).getBytes())
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, String.format("%s.%s",
                        GPX_ID_XML, SOURCE_GPX_XML)));

        String getGpxFilePath = UriComponentsBuilder.fromPath(GET_FILE_PATH)
                .buildAndExpand(SOURCE_GPX_XML, GPX_ID_XML)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(getGpxFilePath, String.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotEmpty();
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    public void getTcxFileTest() {
        // Given

        // Upload xml file
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY,"true");
        Try.of(() -> IOUtils.toString(tcxXmlResource.getInputStream(), UTF_8).getBytes())
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, String.format("%s.%s",
                        TCX_ID_XML, SOURCE_TCX_XML)));

        String getTcxFilePath = UriComponentsBuilder.fromPath(GET_FILE_PATH)
                .buildAndExpand(SOURCE_TCX_XML, TCX_ID_XML)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(getTcxFilePath, String.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotEmpty();
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    public void getIdParamNotCorrectFileTest() {
        // Given
        String badId = "123aaa321";

        String getTcxFilePath = UriComponentsBuilder.fromPath(GET_FILE_PATH)
                .buildAndExpand(SOURCE_TCX_XML, badId)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(getTcxFilePath, String.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getFileNonExistentFileTest() {
        // Given
        String getTcxFilePath = UriComponentsBuilder.fromPath(GET_FILE_PATH)
                .buildAndExpand(SOURCE_TCX_XML, NOT_EXIST_1_ID)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(getTcxFilePath, String.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


}
