package com.routeanalyzer.api.controller.rest;

import com.google.common.collect.Lists;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.routeanalyzer.api.common.TestUtils;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;

import static com.routeanalyzer.api.common.TestUtils.ACTIVITY_GPX_ID;
import static com.routeanalyzer.api.common.TestUtils.ACTIVITY_TCX_ID;
import static com.routeanalyzer.api.common.TestUtils.toActivity;
import static com.routeanalyzer.api.common.TestUtils.toRuntimeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test-mongodb")
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class })
public class ActivityRestControllerTest extends MockMvcTestController {
	@Autowired
	protected ApplicationContext applicationContext;
	@Autowired
	private GpxExportFileService gpxExportFileService;
	@Autowired
	private TcxExportFileService tcxExportFileService;
	@Autowired
	private ActivityOperations activityOperations;
	@Autowired
	private ActivityRestController activityRestController;
	@Autowired
	private ActivityMongoRepository activityMongoRepository;

	private Activity gpxActivity, tcxActivity;

	private static final String ROOT_PATH = "/activity";

	private static final String ERROR_RESPONSE = "Activity was not found or other params are not valid.";

	private static final String GET_ACTIVITY_PATH = ROOT_PATH + "/{id}";
	private static final String EXPORT_AS_PATH = ROOT_PATH + "/{id}/export/{type}";
	private static final String REMOVE_POINT = ROOT_PATH + "/{id}/remove/point";
	private static final String REMOVE_LAP = ROOT_PATH + "/{id}/remove/laps";
	private static final String JOIN_LAPS = ROOT_PATH + "/{id}/join/laps";
	private static final String SPLIT_LAP = ROOT_PATH + "/{id}/split/lap";
	private static final String COLORS_LAP = ROOT_PATH + "/{id}/color/laps";

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

	@Before
	public void setUp() {
		gpxActivity = toActivity(gpxJsonResource).get();
		tcxActivity = toActivity(tcxJsonResource).get();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getGpxActivityByIdTest() throws Exception {
		// Activity does not exists
		isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
		// Activity exists in database
		isReturningActivityHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_GPX_ID), gpxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getTcxActivityByIdTest() throws Exception {
		// Activity does not exists
		isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_GPX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
		// Activity exists in database
		isReturningActivityHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getActivityDataBaseEmptyTest() throws Exception {
		// Activity does not exists
		isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_GPX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
		// Activity exists in database
		isGenerateErrorHTTP(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsTCXThrowExceptionTest() throws Exception {
		String exceptionDescription = "Problem with the file format exported/uploaded.";
		Exception jaxbException =  new JAXBException(exceptionDescription);
		doThrow(toRuntimeException(jaxbException)).when(tcxExportFileService).export(any(Activity.class));
 		isThrowingExceptionHTTP(get(EXPORT_AS_PATH, ACTIVITY_TCX_ID, "tcx"), status().isInternalServerError(),
				exceptionDescription, jaxbException);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsGPXThrowExceptionTest() throws Exception {
		String exceptionDescription = "Problem with the file format exported/uploaded.";
		Exception jaxbException = new JAXBException(exceptionDescription);
		doThrow(toRuntimeException(jaxbException)).when(gpxExportFileService).export(any());
		isThrowingExceptionHTTP(get(EXPORT_AS_PATH, ACTIVITY_GPX_ID, "gpx"), status().isInternalServerError(),
				exceptionDescription, jaxbException);
	}

	@Test
	public void exportAsUnknownXmlTest() throws Exception {
		String unknownXmlType = "kml";
		isGenerateErrorHTTP(get(EXPORT_AS_PATH, ACTIVITY_GPX_ID, unknownXmlType), status().isBadRequest(),
				ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsTCX() throws Exception {
		isReturningFileHTTP(get(EXPORT_AS_PATH, ACTIVITY_TCX_ID, "tcx"), MediaType.APPLICATION_OCTET_STREAM);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsGPX() throws Exception {
		isReturningFileHTTP(get(EXPORT_AS_PATH, ACTIVITY_GPX_ID, "gpx"), MediaType.APPLICATION_OCTET_STREAM);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removeExistingPointTest() throws Exception {
		// Given
		String latitudePointToDelete = "42.6132170";
		String longitudePointToDelete = "-6.5733730";
		String timeInMillisPointToDelete = "1519737378000";
		String indexPointToDelete = "2";
		Activity removePointActivity = toActivity(removePointTcxJsonResource).get();
		// When
		doReturn(removePointActivity).when(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete),
				eq(longitudePointToDelete), eq(timeInMillisPointToDelete), eq(indexPointToDelete));
		isReturningActivityHTTP(put(REMOVE_POINT, ACTIVITY_TCX_ID)
				.param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete)
				.param("timeInMillis", timeInMillisPointToDelete)
				.param("index", indexPointToDelete), removePointActivity);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(removePointActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removePointNonexistentActivityTest() throws Exception {
		String latitudePointToDelete = "42.6131970", longitudePointToDelete = "-6.5732170",
				timeInMillisPointToDelete = "1519737373000", indexPointToDelete = "1";
		isGenerateErrorHTTP(put(REMOVE_POINT, ACTIVITY_TCX_ID)
				.param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete)
				.param("timeInMillis", timeInMillisPointToDelete)
				.param("index", indexPointToDelete), status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removeLapNonexistentActivityTest() throws Exception {
		isGenerateErrorHTTP(put(REMOVE_LAP, ACTIVITY_TCX_ID)
						.param("date", "1519737390000")
						.param("index", "2"),
				status().isBadRequest(), ERROR_RESPONSE, true);
		verify(activityOperations, times(0)).removeLaps(any(), any(), any());
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removeLapActivityTest() throws Exception {
		// Given
		Activity removeLapActivity = toActivity(removeLapTcxJsonResource).get();
		String timeMillis = "1519737373000";
		String index = "1";
		// When
		doReturn(removeLapActivity).when(activityOperations).removeLaps(eq(tcxActivity),
				eq(Lists.newArrayList(Long.parseLong(timeMillis))), eq(Lists.newArrayList(Integer.parseInt(index))));
		isReturningActivityHTTP(
				put(REMOVE_LAP, ACTIVITY_TCX_ID)
						.param("date", timeMillis)
						.param("index", index), removeLapActivity);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(removeLapActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removeLapsActivityTest() throws Exception {
		// Given
		String timeMillis1 = "1519737373000";
		String timeMillis2 = "1519737400000";
		String index1 = "1";
		String index2 = "2";
		Activity lapsRemovedActivity = toActivity(removeLapsTcxJsonResource).get();
		// When
		doReturn(lapsRemovedActivity).when(activityOperations).removeLaps(eq(tcxActivity),
				eq(Lists.newArrayList(Long.parseLong(timeMillis1), Long.parseLong(timeMillis2))),
				eq(Lists.newArrayList(Integer.parseInt(index1), Integer.parseInt(index2))));
		isReturningActivityHTTP(put(REMOVE_LAP, ACTIVITY_TCX_ID)
				.param("date", timeMillis1 + "," + timeMillis2)
				.param("index", index1 + "," + index2), lapsRemovedActivity);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(lapsRemovedActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapsTest() throws Exception {
		// Given
		Activity joinLapsActivity = toActivity(joinLapsTcxJsonResource).get();
		// When
		doReturn(joinLapsActivity).when(activityOperations).joinLaps(any(), eq("0"), eq("1"));
		isReturningActivityHTTP(put(JOIN_LAPS, ACTIVITY_TCX_ID)
						.param("index1", "0")
						.param("index2", "1"), joinLapsActivity);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(joinLapsActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapsForgetParamsTest() throws Exception {
		isGenerateErrorHTTP(put(JOIN_LAPS, ACTIVITY_TCX_ID)
						.param("index1", "")
						.param("index2", ""),
				status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapNonExistentActivityTest() throws Exception {
		isGenerateErrorHTTP(put(JOIN_LAPS, ACTIVITY_TCX_ID)
						.param("index1", "1")
						.param("index2", "2"), status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void splitLapTest() throws Exception {
		// Given
		Activity splitActivity = TestUtils.toActivity(splitTcxJsonResource).get();
		// When
		doReturn(splitActivity).when(activityOperations).splitLap(eq(tcxActivity),
				eq("42.6132170"), eq("-6.5733730"), eq("1519737378000"), eq("2"));
		isReturningActivityHTTP(put(SPLIT_LAP, ACTIVITY_TCX_ID)
				.param("lat", "42.6132170")
				.param("lng", "-6.5733730")
				.param("timeInMillis", "1519737378000")
				.param("index", "2"), splitActivity);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(splitActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void splitLapNonexistentActivityTest() throws Exception {
		isGenerateErrorHTTP(
				put(SPLIT_LAP, ACTIVITY_TCX_ID)
						.param("lat", "42.6132170")
						.param("lng", "-6.5739970")
						.param("timeInMillis", "1519737395000")
						.param("index", "3"),
				status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void splitLapErrorTryingToSplitTest() throws Exception {
		doReturn(null).when(activityOperations).splitLap(any(), any(), any(), any(), any());
		isGenerateErrorHTTP(
				put(SPLIT_LAP, ACTIVITY_TCX_ID)
						.param("lat", "42.6132170")
						.param("lng", "-6.5739970")
						.param("timeInMillis", "1519737395000")
						.param("index", "3"),
				status().isBadRequest(), ERROR_RESPONSE, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void setColorLapsTest() throws Exception {
		// Given
		Activity lapColorsActivity = toActivity(lapColorsTcxJsonResource).get();
		String data = "primero-primero2@segundo-segundo2";
		String description = "Lap's colors are updated.";
		// When
		isGenerateErrorHTTP(put(COLORS_LAP, ACTIVITY_TCX_ID)
				.param("data", data), status().isOk(), description, false);
		// Then
		assertThat(activityMongoRepository.exists(Example.of(lapColorsActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void setColorLapsNonexistentActivityTest() throws Exception {
		String data = "primero-primero2@segundo-segundo2",
				description = "Not being possible to update lap's colors.";
		isGenerateErrorHTTP(put(COLORS_LAP, ACTIVITY_TCX_ID)
						.param("data", data), status().isBadRequest(), description, true);
	}
}
