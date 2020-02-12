package com.routeanalyzer.api.it;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import utils.TestUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.Constants.ACTIVITY_NOT_FOUND;
import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.COLORS_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.EXPORT_AS_PATH;
import static com.routeanalyzer.api.common.Constants.GET_ACTIVITY_PATH;
import static com.routeanalyzer.api.common.Constants.JOIN_LAPS_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_POINT_PATH;
import static com.routeanalyzer.api.common.Constants.SPLIT_LAP_PATH;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

    private Activity gpxActivity, tcxActivity;

    @Before
    public void setUp() {
        gpxActivity = toActivity(gpxJsonResource);
        tcxActivity = toActivity(tcxJsonResource);
    }

    @Test
    public void getGpxActivityByIdTest() throws Exception {
        // Activity with id 5ace8cd14c147400048aa6b0 exists in database
        isReturningActivityHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_GPX_ID), gpxActivity);
    }

    @Test
    public void getTcxActivityByIdTest() throws Exception {
        // Activity with id 5ace8caf4c147400048aa6af exists in database
        isReturningActivityHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID), tcxActivity);
    }

    @Test
    public void getActivityDataBaseEmptyTest() throws Exception {
        isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, NOT_EXIST_1_ID), status().isNotFound(),
                ACTIVITY_NOT_FOUND,true);
        isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, NOT_EXIST_2_ID), status().isNotFound(),
                ACTIVITY_NOT_FOUND, true);
    }

    @Test
    public void exportAsUnknownXmlTest() throws Exception {
        String unknownXmlType = "kml";
        isGenerateErrorHTTP(get(EXPORT_AS_PATH, ACTIVITY_GPX_ID, unknownXmlType), status().isBadRequest(),
                BAD_REQUEST_MESSAGE, true);
    }

    @Test
    public void removeExistingPointTest() throws Exception {
        // Given
        String latitudePointToDelete = "42.6132170";
        String longitudePointToDelete = "-6.5733730";
        String timeInMillisPointToDelete = "1519737378000";
        String indexPointToDelete = "2";

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_1_ID);

        Activity resultRemovedPoint = toActivity(removePointTcxJsonResource);

        // When
        isReturningActivityHTTP(put(REMOVE_POINT_PATH, ACTIVITY_TCX_1_ID)
                .param("lat", latitudePointToDelete)
                .param("lng", longitudePointToDelete)
                .param("timeInMillis", timeInMillisPointToDelete)
                .param("index", indexPointToDelete), resultRemovedPoint);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_1_ID);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void removePointNonexistentActivityTest() throws Exception {
        String latitudePointToDelete = "42.6131970", longitudePointToDelete = "-6.5732170",
                timeInMillisPointToDelete = "1519737373000", indexPointToDelete = "1";
        isGenerateErrorHTTP(put(REMOVE_POINT_PATH, NOT_EXIST_1_ID)
                .param("lat", latitudePointToDelete)
                .param("lng", longitudePointToDelete)
                .param("timeInMillis", timeInMillisPointToDelete)
                .param("index", indexPointToDelete), status().isNotFound(), ACTIVITY_NOT_FOUND, true);
    }

    @Test
    public void removeLapNonExistentActivityTest() throws Exception {
        isGenerateErrorHTTP(put(REMOVE_LAP_PATH, NOT_EXIST_1_ID)
                        .param("date", "1519737390000")
                        .param("index", "2"),
                status().isNotFound(), ACTIVITY_NOT_FOUND, true);
    }

    @Test
    public void removeLapActivityTest() throws Exception {
        // Given
        Activity removeLapActivity = toActivity(removeLapTcxJsonResource);
        String timeMillis = "1519737373000";
        String index = "1";

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_2_ID);

        // When
        isReturningActivityHTTP(
                put(REMOVE_LAP_PATH, ACTIVITY_TCX_2_ID)
                        .param("date", timeMillis)
                        .param("index", index), removeLapActivity);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_2_ID);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void removeLapsActivityTest() throws Exception {
        // Given
        Long timeMillis1 = 1519737373000L;
        Long timeMillis2 = 1519737400000L;
        int index1 = 1;
        int index2 = 2;

        Activity lapsRemovedActivity = toActivity(removeLapsTcxJsonResource);

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_3_ID);

        // When
        isReturningActivityHTTP(put(REMOVE_LAP_PATH, ACTIVITY_TCX_3_ID)
                .param("date", format("%d,%d", timeMillis1, timeMillis2))
                .param("index",format("%d,%d", index1, index2)), lapsRemovedActivity);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_3_ID);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void joinLapsTest() throws Exception {
        // Given
        Activity joinLapsActivity = toActivity(joinLapsTcxJsonResource);
        String index1 = "0";
        String index2 = "1";

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_4_ID);

        // When
        isReturningActivityHTTP(put(JOIN_LAPS_PATH, ACTIVITY_TCX_4_ID)
                .param("index1", index1)
                .param("index2", index2), joinLapsActivity);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_4_ID);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void joinLapsForgetParamsTest() throws Exception {
        isGenerateErrorHTTP(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
                        .param("index1", EMPTY)
                        .param("index2", EMPTY),
                status().isBadRequest(), BAD_REQUEST_MESSAGE, true);
    }

    @Test
    public void joinLapNonExistentActivityTest() throws Exception {
        // Given
        String index1 = "1";
        String index2 = "2";
        // When
        isGenerateErrorHTTP(put(JOIN_LAPS_PATH, NOT_EXIST_1_ID)
                .param("index1", index1)
                .param("index2", index2), status().isNotFound(), ACTIVITY_NOT_FOUND, true);
    }

    @Test
    public void splitLapTest() throws Exception {
        // Given
        Activity splitActivity = TestUtils.toActivity(splitTcxJsonResource);
        String lat = "42.6132170";
        String lng = "-6.5733730";
        String timeMillis = "1519737378000";
        String index = "2";

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_5_ID);

        // When
        isReturningActivityHTTP(put(SPLIT_LAP_PATH, ACTIVITY_TCX_5_ID)
                .param("lat", lat)
                .param("lng", lng)
                .param("timeInMillis", timeMillis)
                .param("index", index), splitActivity);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_5_ID);
        assertThat(afterTestCase).isNotEqualTo(beforeTestCase);
    }

    @Test
    public void splitLapNonexistentActivityTest() throws Exception {
        // Given
        String lat = "42.6132170";
        String lng = "-6.5739970";
        String timeMillis = "1519737395000";
        String index = "3";
        // When
        // Then
        isGenerateErrorHTTP(put(SPLIT_LAP_PATH, NOT_EXIST_1_ID)
                        .param("lat", lat)
                        .param("lng", lng)
                        .param("timeInMillis", timeMillis)
                        .param("index", index),
                status().isNotFound(), ACTIVITY_NOT_FOUND, true);
    }

    @Test
    public void setColorLapsTest() throws Exception {
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

        // When
        isReturningActivityHTTP(put(COLORS_LAP_PATH, ACTIVITY_TCX_6_ID)
                .param("data", data), lapColorsActivity);
        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_6_ID);
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
    }

    @Test
    public void setColorLapsNonexistentActivityTest() throws Exception {
        String data = "abc012-0a1b2c@123abc-0e9d8c";
        isGenerateErrorHTTP(put(COLORS_LAP_PATH, NOT_EXIST_1_ID)
                .param("data", data), status().isNotFound(), ACTIVITY_NOT_FOUND, true);
    }


}
