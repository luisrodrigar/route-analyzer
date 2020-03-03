package com.routeanalyzer.api.facade.impl;

import com.routeanalyzer.api.common.Constants;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityColorsNotAssignedException;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityOperationNoExecutedException;
import io.vavr.control.Try;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;
import static utils.TestUtils.toActivity;

@RunWith(MockitoJUnitRunner.class)
public class ActivityFacadeImplTest {

    @InjectMocks
    private ActivityFacadeImpl activityFacade;
    @Mock
    private ActivityMongoRepository mongoRepository;
    @Mock
    private ActivityOperations activityOperations;

    private static Activity tcxActivity;

    @BeforeClass
    public static void setUp() {
        tcxActivity = toActivity("expected/json-activity-tcx.json");
    }

    @Test
    public void getGpxActivityById() {
        // Given
        Activity gpxActivity = toActivity("expected/json-activity-gpx.json");
        doReturn(of(gpxActivity)).when(mongoRepository).findById(ACTIVITY_GPX_ID);

        // When
        Try<Activity> tryResult = Try.of(() -> activityFacade.getActivityById(ACTIVITY_GPX_ID));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(gpxActivity);

        verify(mongoRepository).findById(eq(ACTIVITY_GPX_ID));
    }

    @Test
    public void getTcxActivityById() {
        // Given
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);

        // When
        Try<Activity> tryResult = Try.of(() -> activityFacade.getActivityById(ACTIVITY_TCX_ID));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(tcxActivity);

        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
    }

    @Test(expected = ActivityNotFoundException.class)
    public void getNonExistentActivityById() throws ActivityNotFoundException {
        // Given
        doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

        // When
        activityFacade.getActivityById(ACTIVITY_TCX_ID);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
    }

    @Test(expected = ActivityNotFoundException.class)
    public void exportAsTcxNonExistentActivity() throws ActivityNotFoundException {
        // Given
        doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

        // When\
        activityFacade.exportAs(ACTIVITY_TCX_ID, Constants.SOURCE_TCX_XML);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
    }

    @Test
    public void exportAsTcx() {
        // Given
        String xmlFile = "Tcx xml file";
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(xmlFile).when(activityOperations).exportByType(eq(SOURCE_TCX_XML), eq(tcxActivity));

        // When
        Try<Optional<String>> result = Try.of(() -> activityFacade.exportAs(ACTIVITY_TCX_ID, SOURCE_TCX_XML));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isNotEmpty();
        assertThat(result.get().get()).isEqualTo(xmlFile);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).exportByType(eq(SOURCE_TCX_XML), eq(tcxActivity));
    }

    @Test
    public void exportAsGpx() {
        // Given
        String xmlFile = "Gpx xml file";

        Activity gpxActivity = toActivity("expected/json-activity-gpx.json");
        doReturn(of(gpxActivity)).when(mongoRepository).findById(ACTIVITY_GPX_ID);
        doReturn(xmlFile).when(activityOperations).exportByType(eq(SOURCE_GPX_XML), eq(gpxActivity));

        // When
        Try<Optional<String>> result = Try.of(() -> activityFacade.exportAs(ACTIVITY_GPX_ID, SOURCE_GPX_XML));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isNotEmpty();
        assertThat(result.get().get()).isEqualTo(xmlFile);
        verify(mongoRepository).findById(eq(ACTIVITY_GPX_ID));
        verify(activityOperations).exportByType(eq(SOURCE_GPX_XML), eq(gpxActivity));
    }

    @Test
    public void removePoint() {
        // Given
        String latitudePointToDelete = "42.6132170";
        String longitudePointToDelete = "-6.5733730";
        Long timeInMillisPointToDelete = 1519737378000L;
        Integer indexPointToDelete = 2;
        Activity removePointActivity = toActivity("expected/activity/remove-point-tcx.json");
        doReturn(of(tcxActivity)).when(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        doReturn(of(removePointActivity)).when(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete),
                eq(longitudePointToDelete), eq(timeInMillisPointToDelete), eq(indexPointToDelete));
        doReturn(removePointActivity).when(mongoRepository).save(eq(removePointActivity));

        // When
        Try<Activity> tryResult = Try.of(() -> activityFacade.removePoint(ACTIVITY_TCX_ID, latitudePointToDelete, longitudePointToDelete,
                timeInMillisPointToDelete, indexPointToDelete));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(removePointActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete), eq(longitudePointToDelete),
                eq(timeInMillisPointToDelete), eq(indexPointToDelete));
    }

    @Test(expected = ActivityNotFoundException.class)
    public void removePointNonExistentActivity() throws ActivityNotFoundException,
            ActivityOperationNoExecutedException {
        // Given
        String latitudePointToDelete = "42.6132170";
        String longitudePointToDelete = "-6.5733730";
        Long timeInMillisPointToDelete = 1519737378000L;
        Integer indexPointToDelete = 2;
        doReturn(empty()).when(mongoRepository).findById(eq(ACTIVITY_TCX_ID));

        // When
        activityFacade.removePoint(ACTIVITY_TCX_ID, latitudePointToDelete, longitudePointToDelete,
                timeInMillisPointToDelete, indexPointToDelete);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).removePoint(any(), any(), any(), any(), any());
        verify(mongoRepository, never()).save(any());
    }

    @Test(expected = ActivityOperationNoExecutedException.class)
    public void removeIssueRemovingPointActivity() throws ActivityNotFoundException,
            ActivityOperationNoExecutedException {
        // Given
        String latitudePointToDelete = "42.6132170";
        String longitudePointToDelete = "-6.5733730";
        Long timeInMillisPointToDelete = 1519737378000L;
        Integer indexPointToDelete = 2;
        doReturn(of(tcxActivity)).when(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        doReturn(empty()).when(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete), eq(longitudePointToDelete),
                eq(timeInMillisPointToDelete), eq(indexPointToDelete));

        // When
        activityFacade.removePoint(ACTIVITY_TCX_ID, latitudePointToDelete, longitudePointToDelete,
                timeInMillisPointToDelete, indexPointToDelete);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).removePoint(eq(tcxActivity), eq(latitudePointToDelete), eq(longitudePointToDelete),
                eq(timeInMillisPointToDelete), eq(indexPointToDelete));
        verify(mongoRepository, never()).save(any());
    }

    @Test(expected = ActivityNotFoundException.class)
    public void removeLapNonExistentActivity() throws ActivityNotFoundException, ActivityOperationNoExecutedException {
        // Give
        Long timeInMillis = 1519737390000L;
        Integer index = 2;
        doReturn(empty()).when(mongoRepository).findById(eq(ACTIVITY_TCX_ID));

        // When
        activityFacade.removeLaps(ACTIVITY_TCX_ID, asList(String.valueOf(timeInMillis)), asList(String.valueOf(index)));

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).removeLaps(any(), any(), any());
        verify(mongoRepository, never()).save(any());
    }

    @Test(expected = ActivityOperationNoExecutedException.class)
    public void removeLapOperationProblemActivity() throws ActivityNotFoundException,
            ActivityOperationNoExecutedException {
        // Give
        Long timeInMillis = 1519737390000L;
        Integer index = 2;
        List<Long> times = asList(timeInMillis);
        List<Integer> indexes = asList(index);
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(empty()).when(activityOperations).removeLaps(tcxActivity, times, indexes);

        // When
        activityFacade.removeLaps(ACTIVITY_TCX_ID, asList(String.valueOf(timeInMillis)), asList(String.valueOf(index)));

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).removeLaps(any(), any(), any());
        verify(mongoRepository, never()).save(any());
    }

    @Test
    public void removeLap() {
        // Given
        String timeMillis = "1519737373000";
        String index = "1";
        List<Long> times = asList(Long.parseLong(timeMillis));
        List<Integer> indexes = asList(Integer.parseInt(index));
        List<String> timesStr = asList(timeMillis);
        List<String> indexesStr = asList(index);
        Activity removeLapActivity = toActivity("expected/activity/remove-lap-tcx.json");
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(of(removeLapActivity)).when(activityOperations).removeLaps(tcxActivity, times, indexes);
        doReturn(removeLapActivity).when(mongoRepository).save(removeLapActivity);

        // When
        Try<Activity> tryResult = Try.of(() -> activityFacade.removeLaps(ACTIVITY_TCX_ID, timesStr, indexesStr));
        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(removeLapActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).removeLaps(eq(tcxActivity), eq(times), eq(indexes));
        verify(mongoRepository).save(eq(removeLapActivity));
    }

    @Test
    public void removeLaps() {
        // Given
		String timeMillis1 = "1519737373000";
		String timeMillis2 = "1519737400000";
		String index1 = "1";
		String index2 = "2";
        List<Long> times = asList(Long.parseLong(timeMillis1), Long.parseLong(timeMillis2));
        List<Integer> indexes = asList(Integer.parseInt(index1), Integer.parseInt(index2));
        List<String> timesStr = asList(timeMillis1, timeMillis2);
        List<String> indexesStr = asList(index1, index2);
		Activity lapsRemovedActivity = toActivity("expected/activity/remove-laps-tcx.json");
		doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(of(lapsRemovedActivity)).when(activityOperations).removeLaps(tcxActivity, times, indexes);
        doReturn(lapsRemovedActivity).when(mongoRepository).save(lapsRemovedActivity);

		// When
		Try<Activity> tryResult = Try.of(() -> activityFacade.removeLaps(ACTIVITY_TCX_ID, timesStr, indexesStr));

		// Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(lapsRemovedActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).removeLaps(eq(tcxActivity), eq(times), eq(indexes));
        verify(mongoRepository).save(eq(lapsRemovedActivity));
    }

    @Test
    public void joinLaps() {
		// Given
		Activity joinLapsActivity = toActivity("expected/activity/join-laps-tcx.json");
		Integer index1 = 0;
		Integer index2 = 1;
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(joinLapsActivity).when(activityOperations).joinLaps(any(), eq(index1), eq(index2));
        doReturn(joinLapsActivity).when(mongoRepository).save(joinLapsActivity);

		// When
        Try<Activity> tryActivity = Try.of(() -> activityFacade.joinLaps(ACTIVITY_TCX_ID, index1, index2));

		// Then
        assertThat(tryActivity.isSuccess()).isTrue();
        assertThat(tryActivity.get()).isEqualTo(joinLapsActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).joinLaps(eq(tcxActivity), eq(index1), eq(index2));
        verify(mongoRepository).save(eq(joinLapsActivity));
    }

    @Test(expected = ActivityNotFoundException.class)
    public void joinLapsActivityNotFound() throws ActivityNotFoundException, ActivityOperationNoExecutedException {
        // Given
        Integer index1 = 1;
        Integer index2 = 2;
        doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

        // When
        activityFacade.joinLaps(ACTIVITY_TCX_ID, index1, index2);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).joinLaps(any(), eq(index1), eq(index2));
        verify(mongoRepository, never()).save(any());
    }

    @Test(expected = ActivityOperationNoExecutedException.class)
    public void joinLapsOperationError() throws ActivityNotFoundException, ActivityOperationNoExecutedException {
        // Given
        Integer index1 = 1;
        Integer index2 = 2;
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(null).when(activityOperations).joinLaps(tcxActivity, index1, index2);

        // When
        activityFacade.joinLaps(ACTIVITY_TCX_ID, index1, index2);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).joinLaps(eq(tcxActivity), eq(index1), eq(index2));
        verify(mongoRepository, never()).save(any());
    }

    @Test
    public void splitLap() {
        // Given
        Activity splitActivity = toActivity("expected/activity/split-lap-tcx.json");
        String lat = "42.6132170";
        String lng = "-6.5733730";
        Long timeMillis = 1519737378000L;
        Integer index = 2;
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(of(splitActivity)).when(activityOperations).splitLap(eq(tcxActivity),
                eq(lat), eq(lng), eq(timeMillis), eq(index));
        doReturn(splitActivity).when(mongoRepository).save(splitActivity);
        // When
        Try<Activity> tryActivity = Try.of(() -> activityFacade.splitLap(ACTIVITY_TCX_ID, lat, lng, timeMillis, index));

        // Then
        assertThat(tryActivity.isSuccess()).isTrue();
        assertThat(tryActivity.get()).isEqualTo(splitActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).splitLap(eq(tcxActivity), eq(lat), eq(lng), eq(timeMillis), eq(index));
        verify(mongoRepository).save(eq(splitActivity));
    }

    @Test(expected = ActivityNotFoundException.class)
    public void splitLapNonExistentActivity() throws ActivityNotFoundException, ActivityOperationNoExecutedException {
        // Given
        String lat = "42.6132170";
        String lng = "-6.5733730";
        Long timeMillis = 1519737378000L;
        Integer index = 2;
        doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

        // When
        activityFacade.splitLap(ACTIVITY_TCX_ID, lat, lng, timeMillis, index);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).splitLap(any(), any(), any(), any(), any());
        verify(mongoRepository, never()).save(any());
    }

    @Test(expected = ActivityOperationNoExecutedException.class)
    public void splitLapOperationNotExecuted() throws ActivityNotFoundException, ActivityOperationNoExecutedException {
        // Given
        String lat = "42.6132170";
        String lng = "-6.5733730";
        Long timeMillis = 1519737378000L;
        Integer index = 2;
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(empty()).when(activityOperations).splitLap(eq(tcxActivity),
                eq(lat), eq(lng), eq(timeMillis), eq(index));
        // When
        activityFacade.splitLap(ACTIVITY_TCX_ID, lat, lng, timeMillis, index);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).splitLap(eq(tcxActivity), eq(lat), eq(lng), eq(timeMillis), eq(index));
        verify(mongoRepository, never()).save(any());
    }

    @Test
    public void setColorLap() {
        // Given
		Activity lapColorsActivity = toActivity("expected/activity/lap-colors-tcx.json");
        String data = "abc012-0a1b2c@123abc-0e9d8c";
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(of(lapColorsActivity)).when(activityOperations).setColorsGetActivity(tcxActivity, data);
        doReturn(lapColorsActivity).when(mongoRepository).save(lapColorsActivity);

		// When
        Try<Activity> tryResult = Try.of(() -> activityFacade.setColorLap(ACTIVITY_TCX_ID, data));

		// Then
		assertThat(tryResult.isSuccess()).isTrue();
		assertThat(tryResult.get()).isEqualTo(lapColorsActivity);
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).setColorsGetActivity(eq(tcxActivity), eq(data));
        verify(mongoRepository).save(eq(lapColorsActivity));
    }



	@Test(expected = ActivityNotFoundException.class)
	public void setColorLapsNonexistentActivityTest() throws ActivityNotFoundException,
            ActivityColorsNotAssignedException {
		// Given
        String data = "primero-primero2@segundo-segundo2";
		doReturn(empty()).when(mongoRepository).findById(ACTIVITY_TCX_ID);

		// When
        activityFacade.setColorLap(ACTIVITY_TCX_ID, data);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations, never()).setColorsGetActivity(any(),any());
        verify(mongoRepository, never()).save(any());
	}

    @Test(expected = ActivityColorsNotAssignedException.class)
    public void setColorLapsErrorHappened() throws ActivityNotFoundException, ActivityColorsNotAssignedException {
        // Given
        String data = "primero-primero2@segundo-segundo2";
        doReturn(of(tcxActivity)).when(mongoRepository).findById(ACTIVITY_TCX_ID);
        doReturn(empty()).when(activityOperations).setColorsGetActivity(tcxActivity, data);

        // When
        activityFacade.setColorLap(ACTIVITY_TCX_ID, data);

        // Then
        verify(mongoRepository).findById(eq(ACTIVITY_TCX_ID));
        verify(activityOperations).setColorsGetActivity(eq(tcxActivity), eq(data));
        verify(mongoRepository, never()).save(any());
    }
}
