package com.routeanalyzer.api.controller.rest;

import com.routeanalyzer.api.controller.ActivityRestController;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletResponse;

import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;
import static utils.TestUtils.toActivity;

@RunWith(SpringJUnit4ClassRunner.class)
public class ActivityRestControllerTest {
	@Mock
	private ActivityMongoRepository mongoRepository;
	@Mock
	private ActivityOperations activityOperationsService;
	@Mock
	private TcxExportFileService tcxExportService;
	@Mock
	private GpxExportFileService gpxExportService;
	@InjectMocks
	public ActivityRestController activityRestController;
	@Mock
	private HttpServletResponse httpServletResponse;

	private Activity gpxActivity, tcxActivity;

	private static final String ERROR_RESPONSE = "Activity was not found or other params are not valid.";

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
	public void getGpxActivityByIdTest() {
		// Given
		doReturn(of(gpxActivity)).when(mongoRepository).findById(ACTIVITY_GPX_ID);

		// When
		Activity activity = activityRestController.getActivityById(ACTIVITY_GPX_ID);

		// Then
		assertThat(activity).isEqualTo(gpxActivity);
		verify(mongoRepository).findById(eq(ACTIVITY_GPX_ID));
	}

	@Test
	public void getTcxActivityByIdTest() throws Exception {
		// Given
		doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);

		// When
		Activity activity = activityRestController.getActivityById(ACTIVITY_TCX_ID);

		// Then
		assertThat(activity).isEqualTo(tcxActivity);
		verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
	}

	@Test(expected = ActivityNotFoundException.class)
	public void getActivityDataBaseEmptyTest() throws Exception {
		// Given
		doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

		// When
		activityRestController.getActivityById(ACTIVITY_TCX_ID);

		// Then
		verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
	}

	@Test(expected = IllegalArgumentException.class)
	public void exportAsTCXThrowExceptionTest() {
		// Given
		String exceptionDescription = "Problem with the file format exported/uploaded.";
		Exception illegalArgumentException =  new IllegalArgumentException(exceptionDescription);
		doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
		doThrow(illegalArgumentException).when(tcxExportService).export(eq(tcxActivity));

		// When
 		activityRestController.exportAs(ACTIVITY_TCX_ID, SOURCE_TCX_XML, httpServletResponse);

 		verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
		verify(tcxExportService).export(eq(tcxActivity));
	}


	@Test(expected = IllegalArgumentException.class)
	public void exportAsUnknownXmlTest() {
		String unknownXmlType = "kml";
		// Given
		String exceptionDescription = "Problem with the input params.";
		Exception illegalArgumentException =  new IllegalArgumentException(exceptionDescription);
		doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);

		// When
		activityRestController.exportAs(ACTIVITY_TCX_ID, unknownXmlType, httpServletResponse);

		verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
	}

	@Test
	public void exportAsTCX() throws Exception {
		// Given
		String xmlFile = "Tcx xml file";
		doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
		doReturn(xmlFile).when(tcxExportService).export(eq(tcxActivity));

		// When
		String result = activityRestController.exportAs(ACTIVITY_TCX_ID, SOURCE_TCX_XML, httpServletResponse);

		// Then
		assertThat(result).isEqualTo(xmlFile);
		verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
		verify(tcxExportService).export(eq(tcxActivity));
		verify(httpServletResponse).addHeader(eq(HttpHeaders.CONTENT_TYPE),
				eq(MediaType.APPLICATION_OCTET_STREAM_VALUE));
		verify(httpServletResponse).addHeader(eq(HttpHeaders.CONTENT_DISPOSITION),
				eq("attachment;filename=" + ACTIVITY_TCX_ID + "_tcx.xml"));
	}

	@Test
	public void exportAsGPX() throws Exception {
		// Given
		String xmlFile = "Gpx xml file";
		doReturn(of(gpxActivity)).when(mongoRepository).findById(ACTIVITY_GPX_ID);
		doReturn(xmlFile).when(tcxExportService).export(eq(gpxActivity));

		// When
		String result = activityRestController.exportAs(ACTIVITY_GPX_ID, SOURCE_GPX_XML, httpServletResponse);

		// Then
		assertThat(result).isEqualTo(xmlFile);
		verify(mongoRepository).findById(eq(ACTIVITY_GPX_ID));
		verify(tcxExportService).export(eq(gpxActivity));
		verify(httpServletResponse).addHeader(eq(HttpHeaders.CONTENT_TYPE),
				eq(MediaType.APPLICATION_OCTET_STREAM_VALUE));
		verify(httpServletResponse).addHeader(eq(HttpHeaders.CONTENT_DISPOSITION),
				eq("attachment;filename=" + ACTIVITY_TCX_ID + "_tcx.xml"));
	}

//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-point.json")
//	public void removeExistingPointTest() throws Exception {
//		// Given
//		String latitudePointToDelete = "42.6132170";
//		String longitudePointToDelete = "-6.5733730";
//		Long timeInMillisPointToDelete = 1519737378000L;
//		Integer indexPointToDelete = 2;
//		Activity removePointActivity = toActivity(removePointTcxJsonResource).get();
//		// When
//		doReturn(removePointActivity).when(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete),
//				eq(longitudePointToDelete), eq(timeInMillisPointToDelete), eq(indexPointToDelete));
//		isReturningActivityHTTP(put(REMOVE_POINT_PATH, ACTIVITY_TCX_ID)
//				.param("lat", latitudePointToDelete)
//				.param("lng", longitudePointToDelete)
//				.param("timeInMillis", String.valueOf(timeInMillisPointToDelete))
//				.param("index", String.valueOf(indexPointToDelete)), removePointActivity);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(removePointActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void removePointNonexistentActivityTest() throws Exception {
//		String latitudePointToDelete = "42.6131970", longitudePointToDelete = "-6.5732170",
//				timeInMillisPointToDelete = "1519737373000", indexPointToDelete = "1";
//		isGenerateErrorHTTP(put(REMOVE_POINT_PATH, ACTIVITY_TCX_ID)
//				.param("lat", latitudePointToDelete)
//				.param("lng", longitudePointToDelete)
//				.param("timeInMillis", timeInMillisPointToDelete)
//				.param("index", indexPointToDelete), status().isBadRequest(), ERROR_RESPONSE, true);
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void removeLapNonexistentActivityTest() throws Exception {
//		isGenerateErrorHTTP(put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
//						.param("date", "1519737390000")
//						.param("index", "2"),
//				status().isBadRequest(), ERROR_RESPONSE, true);
//		verify(activityOperations, times(0)).removeLaps(any(), any(), any());
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-lap.json")
//	public void removeLapActivityTest() throws Exception {
//		// Given
//		Activity removeLapActivity = toActivity(removeLapTcxJsonResource).get();
//		String timeMillis = "1519737373000";
//		String index = "1";
//		// When
//		doReturn(removeLapActivity).when(activityOperations).removeLaps(eq(tcxActivity),
//				eq(Lists.newArrayList(Long.parseLong(timeMillis))), eq(Lists.newArrayList(Integer.parseInt(index))));
//		isReturningActivityHTTP(
//				put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
//						.param("date", timeMillis)
//						.param("index", index), removeLapActivity);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(removeLapActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-without-laps.json")
//	public void removeLapsActivityTest() throws Exception {
//		// Given
//		String timeMillis1 = "1519737373000";
//		String timeMillis2 = "1519737400000";
//		String index1 = "1";
//		String index2 = "2";
//		Activity lapsRemovedActivity = toActivity(removeLapsTcxJsonResource).get();
//		// When
//		doReturn(lapsRemovedActivity).when(activityOperations).removeLaps(eq(tcxActivity),
//				eq(Lists.newArrayList(Long.parseLong(timeMillis1), Long.parseLong(timeMillis2))),
//				eq(Lists.newArrayList(Integer.parseInt(index1), Integer.parseInt(index2))));
//		isReturningActivityHTTP(put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
//				.param("date", timeMillis1 + "," + timeMillis2)
//				.param("index", index1 + "," + index2), lapsRemovedActivity);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(lapsRemovedActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-join-laps.json")
//	public void joinLapsTest() throws Exception {
//		// Given
//		Activity joinLapsActivity = toActivity(joinLapsTcxJsonResource).get();
//		Integer index1 = 0;
//		Integer index2 = 1;
//		// When
//		doReturn(joinLapsActivity).when(activityOperations).joinLaps(any(), eq(index1), eq(index2));
//		isReturningActivityHTTP(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
//						.param("index1", String.valueOf(index1))
//						.param("index2", String.valueOf(index2)), joinLapsActivity);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(joinLapsActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void joinLapsForgetParamsTest() throws Exception {
//		isGenerateErrorHTTP(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
//						.param("index1", EMPTY)
//						.param("index2", EMPTY),
//				status().isBadRequest(), ERROR_RESPONSE, true);
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void joinLapNonExistentActivityTest() throws Exception {
//		// Given
//		String index1 = "1";
//		String index2 = "2";
//		// When
//		isGenerateErrorHTTP(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
//						.param("index1", index1)
//						.param("index2", index2), status().isBadRequest(), ERROR_RESPONSE, true);
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-split-lap.json")
//	public void splitLapTest() throws Exception {
//		// Given
//		Activity splitActivity = TestUtils.toActivity(splitTcxJsonResource).get();
//		String lat = "42.6132170";
//		String lng = "-6.5733730";
//		Long timeMillis = 1519737378000L;
//		Integer index = 2;
//		// When
//		doReturn(splitActivity).when(activityOperations).splitLap(eq(tcxActivity),
//				eq(lat), eq(lng), eq(timeMillis), eq(index));
//		isReturningActivityHTTP(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
//				.param("lat", lat)
//				.param("lng", lng)
//				.param("timeInMillis", String.valueOf(timeMillis))
//				.param("index", String.valueOf(index)), splitActivity);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(splitActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void splitLapNonexistentActivityTest() throws Exception {
//		// Given
//		String lat = "42.6132170";
//		String lng = "-6.5739970";
//		String timeMillis = "1519737395000";
//		String index = "3";
//		// When
//		// Then
//		isGenerateErrorHTTP(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
//						.param("lat", lat)
//						.param("lng", lng)
//						.param("timeInMillis", timeMillis)
//						.param("index", index),
//				status().isBadRequest(), ERROR_RESPONSE, true);
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void splitLapErrorTryingToSplitTest() throws Exception {
//		// Given
//		String lat = "42.6132170";
//		String lng = "-6.5739970";
//		String timeMillis = "1519737395000";
//		String index = "3";
//		//When
//		doReturn(null).when(activityOperations).splitLap(any(), any(), any(), any(), any());
//		// Then
//		isGenerateErrorHTTP(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
//						.param("lat", lat)
//						.param("lng", lng)
//						.param("timeInMillis", timeMillis)
//						.param("index", index),
//				status().isBadRequest(), ERROR_RESPONSE, true);
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-activity-tcx.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	@ShouldMatchDataSet(location = "/controller/db-activity-tcx-lap-colors.json")
//	public void setColorLapsTest() throws Exception {
//		// Given
//		Activity lapColorsActivity = toActivity(lapColorsTcxJsonResource).get();
//		String data = "primero-primero2@segundo-segundo2";
//		String description = "Lap's colors are updated.";
//		// When
//		isGenerateErrorHTTP(put(COLORS_LAP_PATH, ACTIVITY_TCX_ID)
//				.param("data", data), status().isOk(), description, false);
//		// Then
//		assertThat(activityMongoRepository.exists(Example.of(lapColorsActivity))).isTrue();
//	}
//
//	@Test
//	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
//	public void setColorLapsNonexistentActivityTest() throws Exception {
//		String data = "primero-primero2@segundo-segundo2",
//				description = "Not being possible to update lap's colors.";
//		isGenerateErrorHTTP(put(COLORS_LAP_PATH, ACTIVITY_TCX_ID)
//						.param("data", data), status().isBadRequest(), description, true);
//	}
}
