package com.routeanalyzer.api.it;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.DockerComposeContainer;
import utils.TestUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.Constants.COLORS_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.EXPORT_AS_PATH;
import static com.routeanalyzer.api.common.Constants.GET_ACTIVITY_PATH;
import static com.routeanalyzer.api.common.Constants.JOIN_LAPS_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_POINT_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.SPLIT_LAP_PATH;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_1_ID;
import static utils.TestUtils.ACTIVITY_TCX_2_ID;
import static utils.TestUtils.ACTIVITY_TCX_3_ID;
import static utils.TestUtils.ACTIVITY_TCX_4_ID;
import static utils.TestUtils.ACTIVITY_TCX_5_ID;
import static utils.TestUtils.ACTIVITY_TCX_6_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;
import static utils.TestUtils.NOT_EXIST_1_ID;
import static utils.TestUtils.NOT_EXIST_2_ID;
import static utils.TestUtils.toActivity;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
public class ActivityRestControllerIntegrationTest extends IntegrationTest {

    @ClassRule
    public static DockerComposeContainer mongoDbContainer =
            new DockerComposeContainer(new File(DOCKER_COMPOSE_MONGO_DB))
                    .withExposedService(MONGO_CONTAINER_NAME, MONGO_PORT);

    @Autowired
    private ActivityMongoRepository activityMongoRepository;

    @Value("classpath:utils/json-activity-tcx.json")
    private Resource tcxJsonResource;
    @Value("classpath:utils/json-activity-gpx.json")
    private Resource gpxJsonResource;
    @Value("classpath:controller/split-lap-tcx.json")
    private Resource splitTcxJsonResource;
    @Value("classpath:controller/remove-point-tcx.json")
    private Resource removePointTcxJsonResource;
    @Value("classpath:controller/join-laps-tcx.json")
    private Resource joinLapsTcxJsonResource;
    @Value("classpath:controller/lap-colors-tcx.json")
    private Resource lapColorsTcxJsonResource;
    @Value("classpath:controller/remove-lap-tcx.json")
    private Resource removeLapTcxJsonResource;
    @Value("classpath:controller/remove-laps-tcx.json")
    private Resource removeLapsTcxJsonResource;

    private HttpEntity<Activity> requestEntity = new HttpEntity<>(new Activity(), new HttpHeaders());

    @AfterClass
    public static void shutDown() {
        mongoDbContainer.stop();
    }

    @Test
    public void getGpxActivityByIdTest() throws Exception {
        // Given
        Activity gpxActivity = toActivity(gpxJsonResource);
        String getGpxUri = UriComponentsBuilder.fromPath(GET_ACTIVITY_PATH)
                .buildAndExpand(ACTIVITY_GPX_ID)
                .toUriString();
        // When
        // Activity with id 5ace8cd14c147400048aa6b0 exists in database
        ResponseEntity<Activity> result = testRestTemplate.getForEntity(getGpxUri, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(gpxActivity);
    }

    @Test
    public void getTcxActivityByIdTest() {
        // Given
        Activity tcxActivity = toActivity(tcxJsonResource);
        String getTcxUri = UriComponentsBuilder.fromPath(GET_ACTIVITY_PATH)
                .buildAndExpand(ACTIVITY_TCX_ID)
                .toUriString();

        // When
        // Activity with id 5ace8caf4c147400048aa6af exists in database
        ResponseEntity<Activity> result = testRestTemplate.getForEntity(getTcxUri, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(tcxActivity);
    }

    @Test
    public void getActivityDataBaseEmptyTest() {
        // Given
        String getActivityNotFound1 = UriComponentsBuilder.fromPath(GET_ACTIVITY_PATH)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();
        String getActivityNotFound2 = UriComponentsBuilder.fromPath(GET_ACTIVITY_PATH)
                .buildAndExpand(NOT_EXIST_2_ID)
                .toUriString();

        // when
        ResponseEntity<Activity> result1 = testRestTemplate.getForEntity(getActivityNotFound1, Activity.class);
        ResponseEntity<Activity> result2 = testRestTemplate.getForEntity(getActivityNotFound2, Activity.class);

        // Then
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void exportActivityAsGpxTest() {
        // Given
        String exportActivityAsGpxFile = UriComponentsBuilder.fromPath(EXPORT_AS_PATH)
                .buildAndExpand(ACTIVITY_GPX_ID, SOURCE_GPX_XML)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(exportActivityAsGpxFile, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(result.getBody()).isNotEmpty();
    }

    @Test
    public void exportActivityAsTcxTest() {
        // Given
        String exportActivityAsTcxFile = UriComponentsBuilder.fromPath(EXPORT_AS_PATH)
                .buildAndExpand(ACTIVITY_TCX_ID, SOURCE_TCX_XML)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(exportActivityAsTcxFile, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(result.getBody()).isNotEmpty();
    }

    @Test
    public void exportAsNonExistentActivityXmlTest() {
        // Given
        String exportNonExistentActivityToTcxFile = UriComponentsBuilder.fromPath(EXPORT_AS_PATH)
                .buildAndExpand(NOT_EXIST_1_ID, SOURCE_TCX_XML)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(exportNonExistentActivityToTcxFile, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void exportAsUnknownXmlTest() {
        // Given
        String unknownXmlType = "kml";
        String exportAsNonKnownFile = UriComponentsBuilder.fromPath(EXPORT_AS_PATH)
                .buildAndExpand(ACTIVITY_GPX_ID, unknownXmlType)
                .toUriString();

        // When
        ResponseEntity<String> result = testRestTemplate.getForEntity(exportAsNonKnownFile, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void removeExistingPointTest() {
        // Given
        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_1_ID);
        Activity resultRemovedPoint = toActivity(removePointTcxJsonResource);

        String latitudePointToDelete = "42.6132170";
        String longitudePointToDelete = "-6.5733730";
        String timeInMillisPointToDelete = "1519737378000";
        String indexPointToDelete = "2";

        String removePointExistentActivityPath = UriComponentsBuilder.fromPath(REMOVE_POINT_PATH)
                .queryParam("lat", latitudePointToDelete)
                .queryParam("lng", longitudePointToDelete)
                .queryParam("timeInMillis", timeInMillisPointToDelete)
                .queryParam("index", indexPointToDelete)
                .buildAndExpand(ACTIVITY_TCX_1_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removePointExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_1_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(resultRemovedPoint);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);

    }

    @Test
    public void removePointNonexistentActivityTest() {
        // Given
        String latitudePointToDelete = "42.6131970";
        String longitudePointToDelete = "-6.5732170";
        String timeInMillisPointToDelete = "1519737373000";
        String indexPointToDelete = "1";

        String removePointNonExistentActivityPath = UriComponentsBuilder.fromPath(REMOVE_POINT_PATH)
                .queryParam("lat", latitudePointToDelete)
                .queryParam("lng", longitudePointToDelete)
                .queryParam("timeInMillis", timeInMillisPointToDelete)
                .queryParam("index", indexPointToDelete)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removePointNonExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeLapActivityTest() {
        // Given
        Activity removeLapActivity = toActivity(removeLapTcxJsonResource);
        String timeMillis = "1519737373000";
        String index = "1";
        String removeLapExistentActivityPath = UriComponentsBuilder.fromPath(REMOVE_LAP_PATH)
                .queryParam("date", timeMillis)
                .queryParam("index", index)
                .buildAndExpand(ACTIVITY_TCX_2_ID)
                .toUriString();

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_2_ID);

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLapExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_2_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(removeLapActivity);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void removeLapNonExistentActivityTest() {
        // Given
        String timeInMillis = "1519737390000";
        String index = "2";
        String removeLapNonExistentActivityPath = UriComponentsBuilder.fromPath(REMOVE_LAP_PATH)
                .queryParam("date", timeInMillis)
                .queryParam("index", index)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLapNonExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void removeLapsActivityTest() {
        // Given
        Long timeMillis1 = 1519737373000L;
        Long timeMillis2 = 1519737400000L;
        int index1 = 1;
        int index2 = 2;

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_3_ID);

        String removeLapExistentActivityPath = UriComponentsBuilder.fromPath(REMOVE_LAP_PATH)
                .queryParam("date", format("%d,%d", timeMillis1, timeMillis2))
                .queryParam("index", format("%d,%d", index1, index2))
                .buildAndExpand(ACTIVITY_TCX_3_ID)
                .toUriString();

        Activity lapsRemovedActivity = toActivity(removeLapsTcxJsonResource);

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLapExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_3_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(lapsRemovedActivity);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void joinLapsTest() {
        // Given
        Activity joinLapsActivity = toActivity(joinLapsTcxJsonResource);
        String index1 = "0";
        String index2 = "1";

        String removeLapExistentActivityPath = UriComponentsBuilder.fromPath(JOIN_LAPS_PATH)
                .queryParam("index1", index1)
                .queryParam("index2", index2)
                .buildAndExpand(ACTIVITY_TCX_4_ID)
                .toUriString();

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_4_ID);

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLapExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_4_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(joinLapsActivity);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void joinLapsForgetParamsTest() {
        // Given
        String removeLapEmptyParamsPath = UriComponentsBuilder.fromPath(JOIN_LAPS_PATH)
                .queryParam("index1", EMPTY)
                .queryParam("index2", EMPTY)
                .buildAndExpand(ACTIVITY_TCX_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLapEmptyParamsPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void joinLapNonExistentActivityTest() throws Exception {
        // Given
        String index1 = "1";
        String index2 = "2";
        String removeLaNonExistentActivityPath = UriComponentsBuilder.fromPath(JOIN_LAPS_PATH)
                .queryParam("index1", index1)
                .queryParam("index2", index2)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();
        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(removeLaNonExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void splitLapTest() {
        // Given
        Activity splitActivity = TestUtils.toActivity(splitTcxJsonResource);
        String lat = "42.6132170";
        String lng = "-6.5733730";
        String timeMillis = "1519737378000";
        String index = "2";
        String splitLapExistentActivityPath = UriComponentsBuilder.fromPath(SPLIT_LAP_PATH)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("timeInMillis", timeMillis)
                .queryParam("index", index)
                .buildAndExpand(ACTIVITY_TCX_5_ID)
                .toUriString();

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_5_ID);

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(splitLapExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_5_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(splitActivity);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void splitLapNonexistentActivityTest() {
        // Given
        String lat = "42.6132170";
        String lng = "-6.5739970";
        String timeMillis = "1519737395000";
        String index = "3";
        String splitLapNonExistentActivityPath = UriComponentsBuilder.fromPath(SPLIT_LAP_PATH)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .queryParam("timeInMillis", timeMillis)
                .queryParam("index", index)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(splitLapNonExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void setColorLapsTest() {
        // Given
        Activity lapColorsActivity = toActivity(lapColorsTcxJsonResource);
        String data = "abc012-0a1b2c@123abc-0e9d8c";

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_6_ID);
        List<String> beforeColors = beforeTestCase.get().getLaps().stream()
                .map(Lap::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> beforeLightColors = beforeTestCase.get().getLaps().stream()
                .map(Lap::getLightColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String setColorLapsPath = UriComponentsBuilder.fromPath(COLORS_LAP_PATH)
                .queryParam("data", data)
                .buildAndExpand(ACTIVITY_TCX_6_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(setColorLapsPath, HttpMethod.PUT, requestEntity, Activity.class);
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_6_ID);

        // Then
        List<String> afterColors = afterTestCase.get().getLaps().stream()
                .map(Lap::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> afterLightColors = afterTestCase.get().getLaps().stream()
                .map(Lap::getLightColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertThat(beforeColors).isEmpty();
        assertThat(beforeLightColors).isEmpty();
        assertThat(afterColors).isNotEmpty();
        assertThat(afterLightColors).isNotEmpty();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(lapColorsActivity);
    }

    @Test
    public void setColorLapsNonexistentActivityTest() {
        // Given
        String data = "abc012-0a1b2c@123abc-0e9d8c";
        String setColorLapsNonExistentActivityPath = UriComponentsBuilder.fromPath(COLORS_LAP_PATH)
                .queryParam("data", data)
                .buildAndExpand(NOT_EXIST_1_ID)
                .toUriString();

        // When
        ResponseEntity<Activity> result = testRestTemplate
                .exchange(setColorLapsNonExistentActivityPath, HttpMethod.PUT, requestEntity, Activity.class);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


}
