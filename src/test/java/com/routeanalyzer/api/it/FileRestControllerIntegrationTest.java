package com.routeanalyzer.api.it;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import io.vavr.control.Try;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.contract.wiremock.WireMockSpring.options;
import static org.springframework.http.HttpMethod.POST;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static utils.TestUtils.GPX_ID_XML;
import static utils.TestUtils.NOT_EXIST_1_ID;
import static utils.TestUtils.TCX_ID_XML;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
@ContextConfiguration(initializers = FileRestControllerIntegrationTest.Initializer.class)
public class FileRestControllerIntegrationTest extends IntegrationTest {

    private static final String CORUNA_XML_FILE = "coruna.gpx.xml";
    private static final String OVIEDO_XML_FILE = "oviedo.tcx.xml";
    private static final String GPX_WITHOUT_ELEVATION_FILE = "gpx-without-elevation.xml";
    private static final String LOCALHOST_HOST_NAME = "localhost";
    private static final String GOOGLE_MAPS_ENDPOINT_PATH = "/maps/api/elevation/json";
    private static final String API_KEY = "LONG_ENCRYPTED_PASSWORD_FOR_TESTING_PURPOSE_ROUTE_ANALYZER";
    private static final String ENCRYPTED_API_KEY = "BTmeDtfSftCUaPqzpuhPXdLKRpKQkWiGdZLQon+KgztrpZJ/49TL6x6eZVSR5CPQVc0Rz6B1c7lLjX/fvMikvQ==";

    private static final String ELEVATION_ENDPOINT = "/maps/api/elevation/json?locations=%s&key=%s";
    private static final String ELEVATION_STUBBING_RESPONSE = "stubbing-googlemaps-elevations-integration-response.json";
    private static final String POSITIONS_URL_ENCODED = "43.363118916749954,-8.205403927713633%7C43.36308899335563,-8.20535497739911%7C43.36288891732693,-8.205232936888933";
    private static final String BUCKET_NAME_TEST = "route-analyzer-bucket-test";


    @ClassRule
    public static LocalStackContainer localStackS3 = new LocalStackContainer().withServices(S3);

    @ClassRule
    public static WireMockClassRule googleApiWireMockClass = new WireMockClassRule(options()
            .dynamicPort()
            .bindAddress(LOCALHOST_HOST_NAME));

    @ClassRule
    public static DockerComposeContainer mongoDbContainer =
            new DockerComposeContainer(new File(DOCKER_COMPOSE_MONGO_DB))
                    .withExposedService(MONGO_CONTAINER_NAME, MONGO_PORT);

    @Value("classpath:input/coruna.gpx.xml")
    private Resource gpxXmlResource;
    @Value("classpath:input/oviedo.tcx.xml")
    private Resource tcxXmlResource;

    @Autowired
    private OriginalActivityRepository originalActivityRepository;
    @Autowired
    private ActivityMongoRepository activityMongoRepository;

    @AfterClass
    public static void shutDown() {
        mongoDbContainer.stop();
        localStackS3.stop();
        googleApiWireMockClass.stop();
        mongoDbContainer.close();
        localStackS3.close();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "aws.s3-bucket" + BUCKET_NAME_TEST,
                    "google-maps-api.elevation-protocol:http",
                    "google-maps-api.elevation-host: " +
                            format("%s:%d",LOCALHOST_HOST_NAME, googleApiWireMockClass.port()),
                    "google-maps-api.elevation-endpoint: " + GOOGLE_MAPS_ENDPOINT_PATH,
                    "google-maps-api.encrypted-api-key: " + ENCRYPTED_API_KEY
            ).applyTo(ctx);
        }
    }

    @TestConfiguration
    public static class AWSTestConfiguration {

        @Bean
        @Primary
        public AmazonS3 s3client() {
            AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(localStackS3.getEndpointConfiguration(S3))
                    .withCredentials(localStackS3.getDefaultCredentialsProvider())
                    .withPathStyleAccessEnabled(true)
                    .disableChunkedEncoding()
                    .enablePathStyleAccess()
                    .build();
            amazonS3.createBucket(BUCKET_NAME_TEST);
            return amazonS3;
        }

    }

    @Test
    public void uploadGPXFileTest() {
        // Given
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "input", CORUNA_XML_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadGpxXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", SOURCE_GPX_XML)
                .build()
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.exchange(uploadGpxXmlFile, POST, requestEntity, String.class);

        List<String> ids = getIds(result.getBody());
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(format("%s.%s", ids.get(0),
                SOURCE_GPX_XML));

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    @Test
    public void uploadTCXFileTest() {
        // Given
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "input", OVIEDO_XML_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadTcxXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", SOURCE_TCX_XML)
                .build()
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.exchange(uploadTcxXmlFile, POST, requestEntity, String.class);

        List<String> ids = getIds(result.getBody());

        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(format("%s.%s",
                ids.get(0), SOURCE_TCX_XML));

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    @Test
    public void uploadWithoutElevationDataGpxFileTest() {
        // Given
        stubFor(get(urlEqualTo(format(ELEVATION_ENDPOINT, POSITIONS_URL_ENCODED, API_KEY)))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile(ELEVATION_STUBBING_RESPONSE)));

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "input", GPX_WITHOUT_ELEVATION_FILE)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        String uploadGpxXmlFile = UriComponentsBuilder.fromPath(UPLOAD_FILE_PATH)
                .queryParam("type", SOURCE_GPX_XML)
                .build()
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.exchange(uploadGpxXmlFile, POST, requestEntity, String.class);

        List<String> ids = getIds(result.getBody());
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(format("%s.%s", ids.get(0),
                SOURCE_GPX_XML));

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(afterTestCase).isNotEmpty();
        assertThat(afterTestCase.get().getLaps().get(0).getTracks().get(0).getAltitudeMeters())
                .isEqualTo(new BigDecimal("2.200000047683716"));
        assertThat(afterTestCase.get().getLaps().get(0).getTracks().get(1).getAltitudeMeters())
                .isEqualTo(new BigDecimal("1.600000023841858"));
        assertThat(afterTestCase.get().getLaps().get(0).getTracks().get(2).getAltitudeMeters())
                .isEqualTo(new BigDecimal("1.2000000476837158"));
        assertThat(fileStored).isNotEmpty();
    }

    private List<String> getIds(String result) {
        return Stream.of(result.replace("[", "")
                .replace("]","")
                .replace("\"", "")
                .split(","))
                .map(String::trim)
                .collect(toList());
    }

    @Test
    public void uploadUnknownFileTest() {
        // Given
        String unknownXmlFile = "kml";
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new ClassPathResource(format("%s/%s", "input", OVIEDO_XML_FILE)));

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
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, format("%s.%s",
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
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, format("%s.%s",
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
