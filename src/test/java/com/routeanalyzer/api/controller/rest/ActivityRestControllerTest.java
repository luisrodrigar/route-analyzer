package com.routeanalyzer.api.controller.rest;

import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;

import static com.routeanalyzer.api.common.TestUtils.ACTIVITY_GPX_ID;
import static com.routeanalyzer.api.common.TestUtils.ACTIVITY_TCX_ID;
import static com.routeanalyzer.api.common.TestUtils.createGPXActivity;
import static com.routeanalyzer.api.common.TestUtils.createTCXActivity;
import static com.routeanalyzer.api.common.TestUtils.toRuntimeException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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

	private Activity gpxActivity, tcxActivity;

	private static final String ROOT_PATH = "/activity";

	private static final String ERROR_RESPONSE = "Activity was not found or other params are not valid.";

	private static final String EXPORT_AS_PATH = ROOT_PATH + "/{id}/export/{type}";
	private static final String REMOVE_POINT = ROOT_PATH + "/{id}/remove/point";
	private static final String REMOVE_LAP = ROOT_PATH + "/{id}/remove/laps";
	private static final String JOIN_LAPS = ROOT_PATH + "/{id}/join/laps";
	private static final String SPLIT_LAP = ROOT_PATH + "/{id}/split/lap";
	private static final String COLORS_LAP = ROOT_PATH + "/{id}/color/laps";

	@Before
	public void setUp() {
		gpxActivity = createGPXActivity.get();
		tcxActivity = createTCXActivity.get();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getGpxActivityByIdTest() throws Exception {
		String path = ROOT_PATH + "/{id}";
		// Activity does not exists
		isGenerateErrorHTTP(get(path, ACTIVITY_TCX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
		// Activity exists in database
		isReturningActivityHTTP(get(path, ACTIVITY_GPX_ID), gpxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getTcxActivityByIdTest() throws Exception {
		String path = ROOT_PATH + "/{id}";
		// Activity does not exists
		isGenerateErrorHTTP(get(path, ACTIVITY_GPX_ID), status().isBadRequest(), ERROR_RESPONSE, true);
		// Activity exists in database
		isReturningActivityHTTP(get(path, ACTIVITY_TCX_ID), tcxActivity);
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
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx.json")
	public void removeExistingPointTest() throws Exception {
		String latitudePointToDelete = "42.6132120", longitudePointToDelete = "-6.5733020",
				timeInMillisPointToDelete = "1519737376000", indexPointToDelete = "2";
		doReturn(tcxActivity).when(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete),
				eq(longitudePointToDelete), eq(timeInMillisPointToDelete), eq(indexPointToDelete));
		isReturningActivityHTTP(put(REMOVE_POINT, ACTIVITY_TCX_ID)
				.param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete)
				.param("timeInMillis", timeInMillisPointToDelete)
				.param("index", indexPointToDelete), tcxActivity);
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
		String errorDescription = "Given activity id not found in database.";
		isGenerateErrorHTTP(put(REMOVE_LAP, ACTIVITY_TCX_ID)
						.param("date", "1519737390000")
						.param("index", "2"),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-lap.json")
	public void removeLapExistentActivityTest() throws Exception {
		isReturningActivityHTTP(
				put(REMOVE_LAP, ACTIVITY_TCX_ID)
						.param("date", "1519737390000")
						.param("index", "2"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-laps.json")
	public void removeLapsExistentActivityTest() throws Exception {
		isReturningActivityHTTP(put(REMOVE_LAP, ACTIVITY_TCX_ID)
				.param("date", "1519737390000,1519737400000")
				.param("index", "2,3"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-join-laps.json")
	public void joinLapsTest() throws Exception {
		isReturningActivityHTTP(put(JOIN_LAPS, ACTIVITY_TCX_ID)
						.param("index1", "1")
						.param("index2", "2"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapsForgetParamsTest() throws Exception {
		String errorDescription = "Please check the laps indexes params.";
		isGenerateErrorHTTP(put(JOIN_LAPS, ACTIVITY_TCX_ID)
						.param("index1", "")
						.param("index2", ""),
				status().isBadRequest(), errorDescription, true);
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
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-split-lap.json")
	public void splitLapTest() throws Exception {
		isReturningActivityHTTP(put(SPLIT_LAP, ACTIVITY_TCX_ID)
				.param("lat", "42.6132170")
				.param("lng", "-6.5739970")
				.param("timeInMillis", "1519737395000")
				.param("index", "3"), tcxActivity);
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
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-lap-colors.json")
	public void setColorLapsTest() throws Exception {
		String data = "primero-primero2@segundo-segundo2@tercero-tercero2", description = "Lap's colors are updated.";
		isGenerateErrorHTTP(put(COLORS_LAP, ACTIVITY_TCX_ID)
						.param("data", data), status().isOk(), description,
				false);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void setColorLapsNonexistentActivityTest() throws Exception {
		String data = "primero-primero2@segundo-segundo2@tercero-tercero2",
				description = "Not being possible to update lap's colors.";
		isGenerateErrorHTTP(put(COLORS_LAP, ACTIVITY_TCX_ID)
						.param("data", data), status().isBadRequest(), description, true);
	}
}
