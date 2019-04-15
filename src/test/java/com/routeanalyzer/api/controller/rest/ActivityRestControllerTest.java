package com.routeanalyzer.api.controller.rest;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.xml.bind.JAXBException;

import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.common.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test-mongodb")
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class })
public class ActivityRestControllerTest extends MockMvcTestController {
	@Autowired
	protected ApplicationContext applicationContext;
	@Autowired
	private GpxExportFileService gpxService;
	@Autowired
	private TcxExportFileService tcxService;
	@Autowired
	private ActivityOperations activityOperations;

	private Activity gpxActivity, tcxActivity;

	private static final String ROOT_PATH = "/activity";

	private static final String EXPORT_AS_PATH = ROOT_PATH + "/{id}/export/{type}",
			REMOVE_POINT = ROOT_PATH + "/{id}/remove/point", REMOVE_LAP = ROOT_PATH + "/{id}/remove/laps",
			JOIN_LAPS = ROOT_PATH + "/{id}/join/laps", SPLIT_LAP = ROOT_PATH + "/{id}/split/lap",
			COLORS_LAP = ROOT_PATH + "/{id}/color/laps";

	@Before
	public void setUp() {
		gpxActivity = TestUtils.createGPXActivity.get();
		tcxActivity = TestUtils.createTCXActivity.get();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void getActivityByIdTest() throws Exception {
		String path = ROOT_PATH + "/{id}";
		// Activity does not exists
		String errorResponse = "Given activity id not found in database.";
		isGenerateErrorHTTP(get(path, TestUtils.FAKE_ID_TCX), status().isBadRequest(), errorResponse, true);
		// Activity exists in database
		isReturningActivityHTTP(get(path, TestUtils.FAKE_ID_GPX), gpxActivity);
	}

	@Test
	public void exportAsTCXThrowExceptionTest() throws Exception {
		String exceptionDescription = "Syntax error while trying to parse the object.";
		doThrow(new JAXBException(exceptionDescription)).when(tcxService).export(Mockito.any());
		String descriptionErrorValue = "Problem with the file format uploaded.";
		isThrowingExceptionHTTP(get(EXPORT_AS_PATH, TestUtils.FAKE_ID_TCX, "tcx"), status().isInternalServerError(),
				descriptionErrorValue, exceptionDescription);
	}

	@Test
	public void exportAsGPXThrowExceptionTest() throws Exception {
		String exceptionDescription = "Syntax error while trying to parse the object.";
		doThrow(new JAXBException(exceptionDescription)).when(gpxService).export(any());
		String descriptionErrorValue = "Problem with the file format uploaded.";
		isThrowingExceptionHTTP(get(EXPORT_AS_PATH, TestUtils.FAKE_ID_GPX, "gpx"), status().isInternalServerError(),
				descriptionErrorValue, exceptionDescription);
	}

	@Test
	public void exportAsUknownXmlTest() throws Exception {
		String unknownXmlType = "kml", descriptionErrorValue = "Select a correct type for export it.";
		isGenerateErrorHTTP(get(EXPORT_AS_PATH, TestUtils.FAKE_ID_GPX, unknownXmlType), status().isBadRequest(),
				descriptionErrorValue, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsTCX() throws Exception {
		isReturningFileHTTP(get(EXPORT_AS_PATH, TestUtils.FAKE_ID_TCX, "tcx"), MediaType.APPLICATION_OCTET_STREAM);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-gpx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void exportAsGPX() throws Exception {
		isReturningFileHTTP(get(EXPORT_AS_PATH, TestUtils.FAKE_ID_GPX, "gpx"), MediaType.APPLICATION_OCTET_STREAM);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-point.json")
	public void removeExistingPointTest() throws Exception {
		String latitudePointToDelete = "42.6132120", longitudePointToDelete = "-6.5733020",
				timeInMillisPointToDelete = "1519737376000", indexPointToDelete = "2";
		isReturningActivityHTTP(put(REMOVE_POINT, TestUtils.FAKE_ID_TCX).param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete).param("timeInMillis", timeInMillisPointToDelete)
				.param("index", indexPointToDelete), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removePointNonexistentActivityTest() throws Exception {
		String latitudePointToDelete = "42.6131970", longitudePointToDelete = "-6.5732170",
				timeInMillisPointToDelete = "1519737373000", indexPointToDelete = "1";
		String errorDescription = "Given activity not found in database.";
		isGenerateErrorHTTP(put(REMOVE_POINT, TestUtils.FAKE_ID_TCX).param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete).param("timeInMillis", timeInMillisPointToDelete)
				.param("index", indexPointToDelete), status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void removeLapNonexistentActivityTest() throws Exception {
		String errorDescription = "Given activity id not found in database.";
		isGenerateErrorHTTP(put(REMOVE_LAP, TestUtils.FAKE_ID_TCX).param("date", "1519737390000").param("index", "2"),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-lap.json")
	public void removeLapExistentActivityTest() throws Exception {
		isReturningActivityHTTP(
				put(REMOVE_LAP, TestUtils.FAKE_ID_TCX).param("date", "1519737390000").param("index", "2"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-laps.json")
	public void removeLapsExistentActivityTest() throws Exception {
		isReturningActivityHTTP(put(REMOVE_LAP, TestUtils.FAKE_ID_TCX).param("date", "1519737390000,1519737400000")
				.param("index", "2,3"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-join-laps.json")
	public void joinLapsTest() throws Exception {
		isReturningActivityHTTP(put(JOIN_LAPS, TestUtils.FAKE_ID_TCX).param("index1", "1").param("index2", "2"),
				tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapsForgetParamsTest() throws Exception {
		String errorDescription = "Please check the laps indexes params.";
		isGenerateErrorHTTP(put(JOIN_LAPS, TestUtils.FAKE_ID_TCX).param("index1", "").param("index2", ""),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void joinLapNonexistentActivityTest() throws Exception {
		String errorDescription = "Given activity id not found in database.";
		isGenerateErrorHTTP(put(JOIN_LAPS, TestUtils.FAKE_ID_TCX).param("index1", "1").param("index2", "2"),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-split-lap.json")
	public void splitLapTest() throws Exception {
		isReturningActivityHTTP(put(SPLIT_LAP, TestUtils.FAKE_ID_TCX).param("lat", "42.6132170")
				.param("lng", "-6.5739970").param("timeInMillis", "1519737395000").param("index", "3"), tcxActivity);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void splitLapNonexistentActivityTest() throws Exception {
		String errorDescription = "Given activity id not found in database.";
		isGenerateErrorHTTP(
				put(SPLIT_LAP, TestUtils.FAKE_ID_TCX).param("lat", "42.6132170").param("lng", "-6.5739970")
						.param("timeInMillis", "1519737395000").param("index", "3"),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void splitLapErrorTryingToSplitTest() throws Exception {
		String errorDescription = "Response trying split the lap.";
		doReturn(null).when(activityOperations).splitLap(any(), any(), any(), any(), any());
		isGenerateErrorHTTP(
				put(SPLIT_LAP, TestUtils.FAKE_ID_TCX).param("lat", "42.6132170").param("lng", "-6.5739970")
						.param("timeInMillis", "1519737395000").param("index", "3"),
				status().isBadRequest(), errorDescription, true);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-lap-colors.json")
	public void setColorLapsTest() throws Exception {
		String data = "primero-primero2@segundo-segundo2@tercero-tercero2", description = "Lap's colors are updated.";
		isGenerateErrorHTTP(put(COLORS_LAP, TestUtils.FAKE_ID_TCX).param("data", data), status().isOk(), description,
				false);
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void setColorLapsNonexistentActivityTest() throws Exception {
		String data = "primero-primero2@segundo-segundo2@tercero-tercero2",
				description = "Not be posible to update lap's colors.";
		isGenerateErrorHTTP(put(COLORS_LAP, TestUtils.FAKE_ID_TCX).param("data", data), status().isBadRequest(),
				description, true);
	}
}
