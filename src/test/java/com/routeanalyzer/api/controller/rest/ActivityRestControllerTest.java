package com.routeanalyzer.api.controller.rest;

import com.routeanalyzer.api.controller.ActivityRestController;
import com.routeanalyzer.api.facade.ActivityFacade;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityOperationNotExecutedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.routeanalyzer.api.common.Constants.COLORS_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.EXPORT_AS_PATH;
import static com.routeanalyzer.api.common.Constants.GET_ACTIVITY_PATH;
import static com.routeanalyzer.api.common.Constants.JOIN_LAPS_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_POINT_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.SPLIT_LAP_PATH;
import static com.routeanalyzer.api.common.JsonUtils.toJson;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;
import static utils.TestUtils.toActivity;

@RunWith(SpringRunner.class)
@WebMvcTest(ActivityRestController.class)
public class ActivityRestControllerTest {
	@MockBean
	private ActivityFacade activityFacade;

	@Autowired
	private MockMvc mockMvc;

	private Activity gpxActivity, tcxActivity;

	@Value("classpath:expected/json-activity-tcx.json")
	private Resource tcxJsonResource;
	@Value("classpath:expected/json-activity-gpx.json")
	private Resource gpxJsonResource;
	@Value("classpath:expected/activity/split-lap-tcx.json")
	private Resource splitTcxJsonResource;
	@Value("classpath:expected/activity/remove-point-tcx.json")
	private Resource removePointTcxJsonResource;
	@Value("classpath:expected/activity/join-laps-tcx.json")
	private Resource joinLapsTcxJsonResource;
	@Value("classpath:expected/activity/lap-colors-tcx.json")
	private Resource lapColorsTcxJsonResource;
	@Value("classpath:expected/activity/remove-lap-tcx.json")
	private Resource removeLapTcxJsonResource;
	@Value("classpath:expected/activity/remove-laps-tcx.json")
	private Resource removeLapsTcxJsonResource;

	@Before
	public void setUp() {
		gpxActivity = toActivity(gpxJsonResource);
		tcxActivity = toActivity(tcxJsonResource);
	}

	@Test
	public void getGpxActivityByIdTest() throws Exception {
		// Given
		doReturn(gpxActivity).when(activityFacade).getActivityById(ACTIVITY_GPX_ID);

		// When
		// Then
		mockMvc.perform(get(GET_ACTIVITY_PATH, ACTIVITY_GPX_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(gpxActivity)
						.getOrNull()));

		verify(activityFacade).getActivityById(eq(ACTIVITY_GPX_ID));
	}

	@Test
	public void getTcxActivityByIdTest() throws Exception {
		// Given
		doReturn(tcxActivity).when(activityFacade).getActivityById(ACTIVITY_TCX_ID);

		// When
		// Then
		mockMvc.perform(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(tcxActivity)
						.getOrNull()));

		verify(activityFacade).getActivityById(eq(ACTIVITY_TCX_ID));
	}

	@Test
	public void getActivityDataBaseEmptyTest() throws Exception {
		// Given
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID))
				.when(activityFacade).getActivityById(ACTIVITY_TCX_ID);

		// When
		mockMvc.perform(get(GET_ACTIVITY_PATH, ACTIVITY_TCX_ID))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		verify(activityFacade).getActivityById(eq(ACTIVITY_TCX_ID));
	}

	@Test
	public void exportAsTCXThrowExceptionTest() throws Exception {
		// Given
		String exceptionDescription = "Problem with the file format exported/uploaded.";
		Exception illegalArgumentException =  new IllegalArgumentException(exceptionDescription);
		doThrow(illegalArgumentException).when(activityFacade).exportAs(ACTIVITY_TCX_ID, SOURCE_TCX_XML);

		// When
		mockMvc.perform(get(EXPORT_AS_PATH, ACTIVITY_TCX_ID, SOURCE_TCX_XML))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

 		verify(activityFacade).exportAs(eq(ACTIVITY_TCX_ID), eq(SOURCE_TCX_XML));
	}


	@Test
	public void exportAsUnknownXmlTest() throws Exception {
		// Given
		String unknownXmlType = "kml";

		// When
		mockMvc.perform(get(EXPORT_AS_PATH, ACTIVITY_TCX_ID, unknownXmlType))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade, never()).exportAs(eq(ACTIVITY_TCX_ID), eq(unknownXmlType));
	}

	@Test
	public void exportAsTCX() throws Exception {
		// Given
		String xmlFile = "Tcx xml file";
		doReturn(of(xmlFile)).when(activityFacade).exportAs(ACTIVITY_TCX_ID, SOURCE_TCX_XML);

		// When
		mockMvc.perform(get(EXPORT_AS_PATH, ACTIVITY_TCX_ID, SOURCE_TCX_XML))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));

		// Then
		verify(activityFacade).exportAs(eq(ACTIVITY_TCX_ID), eq(SOURCE_TCX_XML));
	}

	@Test
	public void exportAsGPX() throws Exception {
		// Given
		String xmlFile = "Gpx xml file";
		doReturn(of(xmlFile)).when(activityFacade).exportAs(ACTIVITY_GPX_ID, SOURCE_GPX_XML);

		// When
		mockMvc.perform(get(EXPORT_AS_PATH, ACTIVITY_GPX_ID, SOURCE_GPX_XML))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));

		// Then
		verify(activityFacade).exportAs(eq(ACTIVITY_GPX_ID), eq(SOURCE_GPX_XML));
	}

	@Test
	public void removeExistingPointTest() throws Exception {
		// Given
		String latitudePointToDelete = "42.6132170";
		String longitudePointToDelete = "-6.5733730";
		Long timeInMillisPointToDelete = 1519737378000L;
		Integer indexPointToDelete = 2;
		Activity removePointActivity = toActivity(removePointTcxJsonResource);

		doReturn(removePointActivity).when(activityFacade).removePoint(ACTIVITY_TCX_ID, latitudePointToDelete,
				longitudePointToDelete, timeInMillisPointToDelete, indexPointToDelete);

		// When
		// Then
		mockMvc.perform(put(REMOVE_POINT_PATH, ACTIVITY_TCX_ID)
						.param("lat", latitudePointToDelete)
						.param("lng", longitudePointToDelete)
						.param("timeInMillis", String.valueOf(timeInMillisPointToDelete))
						.param("index", String.valueOf(indexPointToDelete)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(removePointActivity)
						.getOrNull()));

		verify(activityFacade).removePoint(eq(ACTIVITY_TCX_ID), eq(latitudePointToDelete),
				eq(longitudePointToDelete), eq(timeInMillisPointToDelete), eq(indexPointToDelete));
	}

	@Test
	public void removePointNonexistentActivityTest() throws Exception {
		// Given
		String latitudePointToDelete = "42.6131970", longitudePointToDelete = "-6.5732170";
		Long timeInMillisPointToDelete = 1519737373000L;
		Integer indexPointToDelete = 1;
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID)).when(activityFacade).removePoint(ACTIVITY_TCX_ID,
				latitudePointToDelete, longitudePointToDelete, timeInMillisPointToDelete, indexPointToDelete);

		// When
		// Then
		mockMvc.perform(put(REMOVE_POINT_PATH, ACTIVITY_TCX_ID)
				.param("lat", latitudePointToDelete)
				.param("lng", longitudePointToDelete)
				.param("timeInMillis", String.valueOf(timeInMillisPointToDelete))
				.param("index", String.valueOf(indexPointToDelete)))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade).removePoint(eq(ACTIVITY_TCX_ID), eq(latitudePointToDelete), eq(longitudePointToDelete),
				eq(timeInMillisPointToDelete), eq(indexPointToDelete));
	}

	@Test
	public void removeLapNonexistentActivityTest() throws Exception {
		// Given
		Long timeInMillisPointToDelete = 1519737373000L;
		String time = String.valueOf(timeInMillisPointToDelete);
		String index = "1";
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID)).when(activityFacade).removeLaps(ACTIVITY_TCX_ID,
				asList(time), asList(index));

		// When
		mockMvc.perform(put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
				.param("date", time)
				.param("index", index))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		verify(activityFacade).removeLaps(eq(ACTIVITY_TCX_ID), eq(asList(time)), eq(asList(index)));
	}

	@Test
	public void removeLapActivityTest() throws Exception {
		// Given
		Activity removeLapActivity = toActivity(removeLapTcxJsonResource);
		String timeMillis = "1519737373000";
		String index = "1";
		doReturn(removeLapActivity).when(activityFacade).removeLaps(ACTIVITY_TCX_ID,
				asList(timeMillis), asList(index));

		// When
		// Then
		mockMvc.perform(put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
				.param("date", timeMillis)
				.param("index", index))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(removeLapActivity).
						getOrNull()));

		verify(activityFacade).removeLaps(eq(ACTIVITY_TCX_ID), eq(asList(timeMillis)), eq(asList(index)));
	}

	@Test
	public void removeLapsActivityTest() throws Exception {
		// Given
		String timeMillis1 = "1519737373000";
		String timeMillis2 = "1519737400000";
		String timeMillis = format("%s,%s", timeMillis1, timeMillis2);
		String index1 = "1";
		String index2 = "2";
		String indexes = format("%s,%s", index1, index2);
		Activity lapsRemovedActivity = toActivity(removeLapsTcxJsonResource);
		doReturn(lapsRemovedActivity).when(activityFacade).removeLaps(eq(ACTIVITY_TCX_ID),
				eq(asList(timeMillis1, timeMillis2)), eq(asList(index1, index2)));
		// When
		// Then
		mockMvc.perform(put(REMOVE_LAP_PATH, ACTIVITY_TCX_ID)
				.param("date", timeMillis)
				.param("index", indexes))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(lapsRemovedActivity)
						.getOrNull()));

		verify(activityFacade).removeLaps(eq(ACTIVITY_TCX_ID), eq(asList(timeMillis1, timeMillis2)),
				eq(asList(index1, index2)));
	}

	@Test
	public void joinLapsTest() throws Exception {
		// Given
		Activity joinLapsActivity = toActivity(joinLapsTcxJsonResource);
		Integer index1 = 0;
		Integer index2 = 1;
		doReturn(joinLapsActivity).when(activityFacade).joinLaps(ACTIVITY_TCX_ID, index1, index2);
		// When
		// Then
		mockMvc.perform(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
				.param("index1", String.valueOf(index1))
				.param("index2", String.valueOf(index2)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(joinLapsActivity)
						.getOrNull()));
		verify(activityFacade).joinLaps(eq(ACTIVITY_TCX_ID), eq(index1), eq(index2));
	}

	@Test
	public void joinLapsForgetEmptyParamsTest() throws Exception {
		// Given
		String index1 = "";
		String index2 = "";
		// When
		// Then
		mockMvc.perform(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
				.param("index1", index1)
				.param("index2",index2))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
		verify(activityFacade, never()).joinLaps(any(String.class), any(Integer.class), any(Integer.class));
	}

	@Test
	public void joinLapsForgetNullParamsTest() throws Exception {
		// Given

		// When
		// Then
		mockMvc.perform(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
				.param("index1","Not a number")
				.param("index2","Not a number"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
		verify(activityFacade, never()).joinLaps(any(String.class), any(Integer.class), any(Integer.class));
	}

	@Test
	public void joinLapNonExistentActivityTest() throws Exception {
		// Given
		String index1 = "1";
		String index2 = "2";
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID)).when(activityFacade).joinLaps(ACTIVITY_TCX_ID,
				Integer.valueOf(index1), Integer.valueOf(index2));
		// When
		mockMvc.perform(put(JOIN_LAPS_PATH, ACTIVITY_TCX_ID)
				.param("index1", index1)
				.param("index2",index2))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
		verify(activityFacade).joinLaps(eq(ACTIVITY_TCX_ID), eq(Integer.valueOf(index1)), eq(Integer.valueOf(index2)));
	}

	@Test
	public void splitLapTest() throws Exception {
		// Given
		Activity splitActivity = toActivity(splitTcxJsonResource);
		String lat = "42.6132170";
		String lng = "-6.5733730";
		Long timeMillis = 1519737378000L;
		Integer index = 2;
		doReturn(splitActivity).when(activityFacade).splitLap(ACTIVITY_TCX_ID, lat, lng, timeMillis, index);
		// When
		// Then
		mockMvc.perform(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
				.param("lat", lat)
				.param("lng",lng)
				.param("timeInMillis", String.valueOf(timeMillis))
				.param("index", String.valueOf(index)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade).splitLap(eq(ACTIVITY_TCX_ID), eq(lat), eq(lng), eq(timeMillis), eq(index));
	}

	@Test
	public void splitLapNonexistentActivityTest() throws Exception {
		// Given
		String lat = "42.6132170";
		String lng = "-6.5739970";
		String timeMillis = "1519737395000";
		String index = "3";
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID)).when(activityFacade).splitLap(ACTIVITY_TCX_ID, lat, lng,
				Long.valueOf(timeMillis), Integer.valueOf(index));

		// When
		// Then
		mockMvc.perform(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
				.param("lat", lat)
				.param("lng",lng)
				.param("timeInMillis", timeMillis)
				.param("index", index))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade).splitLap(eq(ACTIVITY_TCX_ID), eq(lat), eq(lng), eq(Long.valueOf(timeMillis)),
				eq(Integer.valueOf(index)));
	}

	@Test
	public void splitLapErrorTryingToSplitTest() throws Exception {
		// Given
		String lat = "42.6132170";
		String lng = "-6.5739970";
		String timeMillis = "1519737395000";
		String index = "3";
		doThrow(new ActivityOperationNotExecutedException(ACTIVITY_TCX_ID, "splitLap"))
				.when(activityFacade).splitLap(ACTIVITY_TCX_ID, lat, lng, Long.valueOf(timeMillis),
				Integer.valueOf(index));
		//When
		// Then
		mockMvc.perform(put(SPLIT_LAP_PATH, ACTIVITY_TCX_ID)
				.param("lat", lat)
				.param("lng",lng)
				.param("timeInMillis", timeMillis)
				.param("index", index))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
		verify(activityFacade).splitLap(eq(ACTIVITY_TCX_ID), eq(lat), eq(lng), eq(Long.valueOf(timeMillis)),
				eq(Integer.valueOf(index)));
	}

	@Test
	public void setColorLapsTest() throws Exception {
		// Given
		Activity lapColorsActivity = toActivity(lapColorsTcxJsonResource);
		String data = "A01C34-EE1122@010ACB-A01B02";
		String description = "Lap's colors are updated.";
		doReturn(lapColorsActivity).when(activityFacade).setColorLap(ACTIVITY_TCX_ID, data);
		// When
		// Then
		mockMvc.perform(put(COLORS_LAP_PATH, ACTIVITY_TCX_ID)
				.param("data", data))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(toJson(lapColorsActivity)
						.getOrNull()));

		verify(activityFacade).setColorLap(eq(ACTIVITY_TCX_ID), eq(data));
	}

	@Test
	public void setColorLapsNotFoundActivityTest() throws Exception {
		// Given
		String data = "a01c34-ee1122@010acb-a01b02";
		doThrow(new ActivityNotFoundException(ACTIVITY_TCX_ID)).when(activityFacade).setColorLap(ACTIVITY_TCX_ID, data);
		// When
		// Then
		mockMvc.perform(put(COLORS_LAP_PATH, ACTIVITY_TCX_ID)
				.param("data", data))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade).setColorLap(eq(ACTIVITY_TCX_ID), eq(data));
	}

	@Test
	public void setColorLapsErrorInputParamsTest() throws Exception {
		// Given
		String data = "primero-primero2@segundo-segundo2";
		// When
		// Then
		mockMvc.perform(put(COLORS_LAP_PATH, ACTIVITY_TCX_ID)
				.param("data", data))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		verify(activityFacade, never()).setColorLap(eq(ACTIVITY_TCX_ID), eq(data));
	}
}
