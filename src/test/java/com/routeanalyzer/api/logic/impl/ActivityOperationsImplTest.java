package com.routeanalyzer.api.logic.impl;

import com.google.common.collect.Lists;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.DateUtils.toZonedDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class ActivityOperationsImplTest {

	@Mock
	private LapsOperations lapsOperations;
	@InjectMocks
	private ActivityOperationsImpl activityOperations;

	private Activity activity;
	private Lap lap1;
	private Lap lap2;
	private Lap lap3;
	private Lap lap4;
	private Lap lap3OneTrackPoint;
	private List<Lap> laps;
	private long timeMillisLap11, timeMillisLap12, timeMillisLap21, timeMillisLap22, timeMillisLap31, timeMillisLap32,
			timeMillisLap41, timeMillisLap42;
	private int index11, index12, index21, index22, index31, index32, index41, index42;

	@Before
	public void setUp() {
		timeMillisLap11 = 123456L;
		timeMillisLap12 = 123466L;
		timeMillisLap21 = 123476L;
		timeMillisLap22 = 123486L;
		timeMillisLap31 = 123506L;
		timeMillisLap32 = 123526L;
		timeMillisLap41 = 123546L;
		timeMillisLap42 = 123606L;
        index11 = 0;
        index12 = 1;
        index21 = 2;
        index22 = 3;
        index31 = 4;
        index32 = 5;
        index41 = 6;
        index42 = 7;
		laps = Lists.newArrayList();
		initLaps();
		addTracksToLaps();
	}
	private void initLaps() {
		lap1 = Lap.builder()
				.distanceMeters(100.0)
				.startTime(toZonedDateTime(timeMillisLap11).orElse(null))
				.index(0)
				.totalTimeSeconds(50.0)
				.intensity("LOW")
				.build();
		lap2 = Lap.builder()
				.distanceMeters(150.0)
				.startTime(toZonedDateTime(timeMillisLap21).orElse(null))
				.index(1)
				.totalTimeSeconds(50.0)
				.intensity("MEDIUM")
				.build();
		lap3 = Lap.builder()
				.distanceMeters(100.0)
				.startTime(toZonedDateTime(timeMillisLap31).orElse(null))
				.index(2)
				.totalTimeSeconds(50.0)
				.intensity("HIGH")
				.build();
		lap4 = Lap.builder()
				.distanceMeters(150.0)
				.startTime(toZonedDateTime(timeMillisLap41).orElse(null))
				.index(3)
				.totalTimeSeconds(50.0)
				.intensity("HIGH")
				.build();
        lap3OneTrackPoint = Lap.builder()
                .distanceMeters(100.0)
                .startTime(toZonedDateTime(timeMillisLap31).orElse(null))
                .index(2)
                .totalTimeSeconds(50.0)
                .intensity("HIGH")
                .build();
	}

	private void addTracksToLaps(){
		createAddTrack(lap1, timeMillisLap11, timeMillisLap12, index11, index12,
				"43.3602900","43.352478",  "-5.8447600", "-5.8501170", "120",
				"123", "25.0", "25.0", "12.0", "12.0", 76, 86);
		lap1.setStartTime(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null));
		createAddTrack(lap2, timeMillisLap21, timeMillisLap22, index21, index22,
				"44.3602900","46.352478",  "-6.8447600", "-4.8501170", "119",
				"125", "15.0", "15.0", "16.0", "13.0", 90, 95);
		lap2.setStartTime(DateUtils.toZonedDateTime(timeMillisLap21).orElse(null));
		createAddTrack(lap3, timeMillisLap31, timeMillisLap32, index31, index32,
				"42.3602900","46.452478",  "-3.8447600", "-6.9501170", "130",
				"131", "46.0", "65.0", "21.0", "10.0", 100, 107);
		lap3.setStartTime(DateUtils.toZonedDateTime(timeMillisLap31).orElse(null));
		createAddTrack(lap4, timeMillisLap41, timeMillisLap42, index41, index42,
				"40.3602900","40.352478",  "-8.8447600", "-9.8501170", "116",
				"121", "80.0", "120.0", "13.0", "15.0", 112, 123);
		lap4.setStartTime(DateUtils.toZonedDateTime(timeMillisLap41).orElse(null));
		lap3OneTrackPoint.setStartTime(DateUtils.toZonedDateTime(timeMillisLap31).orElse(null));
        createAddTrack(lap3OneTrackPoint, timeMillisLap31, timeMillisLap32, index31, index32,
                "42.3602900","46.452478",  "-3.8447600", "-6.9501170", "130",
                "131", "46.0", "65.0", "21.0", "10.0", 100, 107);
        lap3OneTrackPoint.getTracks().remove(1);
	}

	private void createAddTrack(Lap lap, long timeMillisLap1,
								 long timeMillisLap2, int index1, int index2, String latitude1, String latitude2,
								 String lng1, String lng2, String alt1, String alt2, String dist1, String dist2,
								 String speed1, String speed2, int heartRate1, int heartRate2) {
		List<TrackPoint> trackPoints = Lists.newArrayList();
		trackPoints.add(toTrackPoint(timeMillisLap1, index1, latitude1, lng1, alt1, dist1, speed1, heartRate1));
		trackPoints.add(toTrackPoint(timeMillisLap2, index2, latitude2, lng2, alt2, dist2, speed2, heartRate2));
		lap.setTracks(trackPoints);
	}


	@Test
	public void removeLapTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap21),
				Lists.newArrayList(1));
		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(activity);
		assertThat(result.getLaps()).isNotEmpty();
		assertThat(result.getLaps().size()).isEqualTo(3);
		assertThat(result.getLaps()).doesNotContain(lap2);
	}

	@Test
	public void removeLapNullStartTimeTest() {
		// Given
		laps.add(lap1);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, null, Lists.newArrayList(0));
		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(activity);
		assertThat(result.getLaps()).doesNotContain(lap1);
		assertThat(result.getLaps()).isEmpty();
	}

	@Test
	public void removeLapsEmptyLapsTest() {
		// Given
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap21), Lists.newArrayList(1));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isEmpty();
	}

	@Test
	public void removeLapsIndexBigLessLapSizeTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap21),
				Lists.newArrayList(120));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotEmpty();
		assertThat(result.getLaps().size()).isEqualTo(4);
	}

	@Test
	public void removeLapsIndexNegTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap21),
				Lists.newArrayList(-1));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotEmpty();
		assertThat(result.getLaps().size()).isEqualTo(4);
	}

	@Test
	public void removeLapsStartTimeNullTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, null, Lists.newArrayList(-1));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotEmpty();
		assertThat(result.getLaps().size()).isEqualTo(4);
	}

	@Test
	public void removeLapsIndexNullTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap11), null);
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotEmpty();
		assertThat(result.getLaps().size()).isEqualTo(4);
	}

	@Test
	public void removeLapsActNullTest() {
		// Given
		activity = null;
		// When
		Activity result = activityOperations.removeLaps(activity, Lists.newArrayList(timeMillisLap11), null);
		// Then
		assertThat(result).isNull();
	}

	@Test
	public void joinLapsTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		Lap joinedLap = Lap.builder().build();
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		doReturn(joinedLap).when(lapsOperations).joinLaps(eq(lap2), eq(lap3));
		Activity result = activityOperations.joinLaps(activity, 1, 2);
		// Then
		verify(lapsOperations).joinLaps(eq(lap2), eq(lap3));
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotNull();
		List<Lap> laps = result.getLaps();
		assertThat(laps.size()).isEqualTo(3);
		assertThat(laps.get(1)).isEqualTo(joinedLap);
		assertThat(laps.get(2)).isEqualTo(lap4);
		assertThat(laps.get(2).getIndex()).isEqualTo(2);
	}

	@Test
	public void joinLapsSwapIndexesTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		Lap joinedLap = Lap.builder().build();
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		doReturn(joinedLap).when(lapsOperations).joinLaps(eq(lap2), eq(lap3));
		Activity result = activityOperations.joinLaps(activity, 2, 1);
		// Then
		verify(lapsOperations).joinLaps(eq(lap2), eq(lap3));
		assertThat(result).isNotNull();
		assertThat(result.getLaps()).isNotNull();
		List<Lap> laps = result.getLaps();
		assertThat(laps.size()).isEqualTo(3);
		assertThat(laps.get(1)).isEqualTo(joinedLap);
		assertThat(laps.get(2)).isEqualTo(lap4);
		assertThat(laps.get(2).getIndex()).isEqualTo(2);
	}

	private void joinLaps(Integer indexLeft, Integer indexRight) {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity result = activityOperations.joinLaps(activity, indexLeft, indexRight);
		// Then
		verify(lapsOperations, times(0)).joinLaps(any(), any());
		assertThat(result).isNull();
	}

	@Test
	public void indexOfTrackPointTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		Position position = toPosition("46.452478", "-6.9501170");
		// When
		doReturn(lap3.getTracks().get(1)).when(lapsOperations)
				.getTrackPoint(eq(lap3),eq(position), eq(timeMillisLap32), eq(1));
		int index = activityOperations.indexOfTrackPoint(activity, 2, position, timeMillisLap32, 1);
		// Then
		verify(lapsOperations, times(1)).getTrackPoint(any(), any(), any(), any());
		assertThat(index).isPositive();
		assertThat(index).isEqualTo(1);
	}

	@Test
	public void indexOfTrackPointNullTimeMillisTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		Position position = toPosition("46.452478", "-6.9501170");
		// When
		doReturn(lap3.getTracks().get(1)).when(lapsOperations)
				.getTrackPoint(eq(lap3),eq(position), isNull(), eq(1));
		int index = activityOperations.indexOfTrackPoint(activity, 2, position, null, 1);
		// Then
		verify(lapsOperations, times(1)).getTrackPoint(any(), any(), any(), any());
		assertThat(index).isPositive();
		assertThat(index).isEqualTo(1);
	}

	@Test
	public void indexOfTrackPointNullIndexTrackPointTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		Position position = toPosition("46.452478", "-6.9501170");
		// When
		doReturn(lap3.getTracks().get(1)).when(lapsOperations)
				.getTrackPoint(eq(lap3),eq(position), eq(timeMillisLap31), isNull());
		int index = activityOperations.indexOfTrackPoint(activity, 2, position, timeMillisLap31, null);
		// Then
		verify(lapsOperations, times(1)).getTrackPoint(any(), any(), any(), any());
		assertThat(index).isPositive();
		assertThat(index).isEqualTo(1);
	}

	@Test
	public void indexOfTrackPointNullPositionTest() {
		// Given
		laps.add(lap1);
		laps.add(lap2);
		laps.add(lap3);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		Position position = null;
		// When
		doReturn(lap3.getTracks().get(1)).when(lapsOperations)
				.getTrackPoint(eq(lap3), isNull(), eq(timeMillisLap31), eq(1));
		int index = activityOperations.indexOfTrackPoint(activity, 2, position, timeMillisLap31, 1);
		// Then
		verify(lapsOperations, times(1)).getTrackPoint(any(), any(), any(), any());
		assertThat(index).isPositive();
		assertThat(index).isEqualTo(1);
	}

	@Test
	public void indexOfTrackPointNullActivityTest() {
		// Given
		activity = null;
		Position position = toPosition("46.452478", "-6.9501170");
		int index = activityOperations.indexOfTrackPoint(activity, 2, position, timeMillisLap31, 1);
		// Then
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
		assertThat(index).isNegative();
		assertThat(index).isEqualTo(-1);
	}

	@Test
    public void removePointTest() {
	    // Given
        laps.add(lap1);
        laps.add(lap2);
        laps.add(lap3);
        laps.add(lap4);
        activity = Activity.builder()
                .laps(laps)
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        Position position = toPosition("46.452478", "-6.9501170");
        // When
        doReturn(true).when(lapsOperations)
                .fulfillCriteriaPositionTime(eq(lap3), eq(position), eq(timeMillisLap32), eq(index32));
        doReturn(lap3.getTracks().get(1)).when(lapsOperations)
                .getTrackPoint(eq(lap3), eq(position), eq(timeMillisLap32), eq(index32));
        Activity act = activityOperations.removePoint(activity, "46.452478", "-6.9501170",
                timeMillisLap32, index32);

        // Then
		verify(lapsOperations).getTrackPoint(any(), any(), any(), any());
		verify(lapsOperations, times(3)).fulfillCriteriaPositionTime(any(), any(), any(), any());
        assertThat(act).isNotNull();
        assertThat(act.getLaps()).isNotEmpty();
        assertThat(act.getLaps().get(2)).isNotNull();
        Lap lap = act.getLaps().get(2);
        assertThat(lap.getTracks()).isNotEmpty();
        assertThat(lap.getTracks().size()).isEqualTo(1);
    }

    @Test
    public void removePointOneTrackInLapTest() {
        // Given
        laps.add(lap1);
        laps.add(lap2);
        laps.add(lap3OneTrackPoint);
        laps.add(lap4);
        activity = Activity.builder()
                .laps(laps)
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        Position position = toPosition("40.3602900", "-8.8447600");
        // When
        doReturn(true).when(lapsOperations)
                .fulfillCriteriaPositionTime(eq(lap3OneTrackPoint), eq(position), eq(timeMillisLap31), eq(index31));
        doReturn(lap3OneTrackPoint.getTracks().get(0)).when(lapsOperations)
                .getTrackPoint(eq(lap3OneTrackPoint), eq(position), eq(timeMillisLap31), eq(index31));
        Activity act = activityOperations.removePoint(activity, "40.3602900", "-8.8447600",
                timeMillisLap31, index31);

        // Then
		verify(lapsOperations).getTrackPoint(any(), any(), any(), any());
		verify(lapsOperations, times(3)).fulfillCriteriaPositionTime(any(), any(), any(), any());
        assertThat(act).isNotNull();
        assertThat(act.getLaps()).isNotEmpty();
        assertThat(act.getLaps().get(2)).isNotNull();
        assertThat(act.getLaps().size()).isEqualTo(3);
        Lap lap = act.getLaps().get(2);
        assertThat(lap).isEqualTo(lap4);
        assertThat(lap.getTracks().size()).isEqualTo(2);
    }

    @Test
    public void removePointEmptyLapsInLapTest() {
        // Given
        activity = Activity.builder()
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        // When
        Activity act = activityOperations.removePoint(activity, "40.3602900", "-8.8447600",
                timeMillisLap31, index31);

        // Then
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
		verify(lapsOperations, times(0)).fulfillCriteriaPositionTime(any(), any(), any(), any());
        verify(lapsOperations, times(0)).fulfillCriteriaPositionTime(any(), any(), any(), any());
        verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
        assertThat(act).isNull();
    }

    @Test
    public void removePointNullTimeMillisTest() {
        // Given
        laps.add(lap1);
        laps.add(lap2);
        laps.add(lap3OneTrackPoint);
        laps.add(lap4);
        activity = Activity.builder()
                .laps(laps)
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        Position position = toPosition("40.3602900", "-8.8447600");
        // When
        doReturn(true).when(lapsOperations)
                .fulfillCriteriaPositionTime(eq(lap3OneTrackPoint), eq(position), isNull(), eq(index31));
        doReturn(lap3OneTrackPoint.getTracks().get(0)).when(lapsOperations)
                .getTrackPoint(eq(lap3OneTrackPoint), eq(position), isNull(), eq(index31));
        Activity act = activityOperations.removePoint(activity, "40.3602900", "-8.8447600",
                null, index31);

        // Then
		verify(lapsOperations).getTrackPoint(any(), any(), isNull(), any());
		verify(lapsOperations, times(3))
				.fulfillCriteriaPositionTime(any(), any(), isNull(), any());
        assertThat(act).isNotNull();
        assertThat(act.getLaps()).isNotEmpty();
        assertThat(act.getLaps().get(2)).isNotNull();
        assertThat(act.getLaps().size()).isEqualTo(3);
        Lap lap = act.getLaps().get(2);
        assertThat(lap).isEqualTo(lap4);
        assertThat(lap.getTracks().size()).isEqualTo(2);
    }

    @Test
    public void removePointNullIndexTest() {
        // Given
        laps.add(lap1);
        laps.add(lap2);
        laps.add(lap3);
        laps.add(lap4);
        activity = Activity.builder()
                .laps(laps)
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        Position position = toPosition("44.3602900",  "-6.8447600");
        // When
        doReturn(true).when(lapsOperations)
                .fulfillCriteriaPositionTime(eq(lap2), eq(position), eq(timeMillisLap21), isNull());
        doReturn(lap2.getTracks().get(0)).when(lapsOperations)
                .getTrackPoint(eq(lap2), eq(position), eq(timeMillisLap21), isNull());
        Activity act = activityOperations.removePoint(activity, "44.3602900",  "-6.8447600",
                timeMillisLap21, null);

        // Then
		verify(lapsOperations).getTrackPoint(any(), any(), any(), isNull());
		verify(lapsOperations, times(2))
				.fulfillCriteriaPositionTime(any(), any(), any(), isNull());
        assertThat(act).isNotNull();
        assertThat(act.getLaps()).isNotEmpty();
        assertThat(act.getLaps().get(1)).isNotNull();
        Lap lap = act.getLaps().get(1);
        assertThat(lap.getTracks()).isNotEmpty();
        assertThat(lap.getTracks().get(0)).isEqualTo(lap2.getTracks().get(1));
        assertThat(lap.getTracks().size()).isEqualTo(1);
    }

    @Test
    public void removePointNullIndexAndTimeMillisTest() {
        // Given
        laps.add(lap1);
        laps.add(lap2);
        laps.add(lap3);
        laps.add(lap4);
        activity = Activity.builder()
                .laps(laps)
                .idUser("foo")
                .name("boo")
                .sport("sport")
                .date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
                .build();
        // When
        Activity act = activityOperations.removePoint(activity, "46.452478", "-6.9501170",
               null, null);

        // Then
		verify(lapsOperations, times(4)).fulfillCriteriaPositionTime(any(), any(), isNull(), isNull());
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
        assertThat(act).isNull();
    }

    @Test
	public void splitLapTest() {
		// Given
		lap1.getTracks().addAll(lap2.getTracks());
		laps.add(lap1);
		lap3.setIndex(1);
		laps.add(lap3);
		lap4.setIndex(2);
		laps.add(lap4);
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		Position position = toPosition("44.3602900", "-6.8447600");
		// When
		doReturn(true).when(lapsOperations)
				.fulfillCriteriaPositionTime(eq(lap1), eq(position), eq(timeMillisLap21), eq(index21));
		doReturn(lap2.getTracks().get(0)).when(lapsOperations)
				.getTrackPoint(eq(lap1), eq(position), eq(timeMillisLap21), eq(index21));
		doReturn(lap3OneTrackPoint).when(lapsOperations).createSplitLap(eq(lap1), eq(0), eq(index21), eq(lap1.getIndex()));
		doReturn(lap2).when(lapsOperations).createSplitLap(eq(lap1), eq(index21), eq(lap1.getTracks().size()), eq(lap1.getIndex() + 1));
		Activity act = activityOperations.splitLap(activity, "44.3602900", "-6.8447600",
				timeMillisLap21, index21);

		// Then
		verify(lapsOperations, times(1)).fulfillCriteriaPositionTime(any(), any(), any(), any());
		verify(lapsOperations, times(1)).getTrackPoint(any(), any(), any(), any());
		assertThat(act).isNotNull();
		assertThat(act.getLaps()).isNotEmpty();
		List<Lap> laps = act.getLaps();
		assertThat(laps.size()).isEqualTo(4);
		assertThat(laps.get(0)).isEqualTo(lap3OneTrackPoint);
		assertThat(laps.get(1)).isEqualTo(lap2);
		assertThat(laps.get(2).getIndex()).isEqualTo(lap1.getIndex() + 2);
		assertThat(laps.get(3).getIndex()).isEqualTo(lap1.getIndex() + 3);
	}

	@Test
	public void splitLapNullActivityTest() {
		// Given activity null
		// When
		Activity act = activityOperations.splitLap(null, "44.3602900", "-6.8447600",
				timeMillisLap21, index21);

		// Then
		verify(lapsOperations, times(0)).fulfillCriteriaPositionTime(any(), any(), any(), any());
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
		assertThat(act).isNull();
	}

	@Test
	public void splitLapNullLatParamTest() {
		// Given
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity act = activityOperations.splitLap(activity, null, "-6.8447600", timeMillisLap21, index21);

		// Then
		verify(lapsOperations, times(0)).fulfillCriteriaPositionTime(any(), any(), any(), any());
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
		assertThat(act).isNull();
	}

	@Test
	public void splitLapNullLngParamTest() {
		// Given
		activity = Activity.builder()
				.laps(laps)
				.idUser("foo")
				.name("boo")
				.sport("sport")
				.date(DateUtils.toZonedDateTime(timeMillisLap11).orElse(null))
				.build();
		// When
		Activity act = activityOperations.splitLap(activity, "44.3602900", null,
				timeMillisLap21, index21);

		// Then
		verify(lapsOperations, times(0)).fulfillCriteriaPositionTime(any(), any(), any(), any());
		verify(lapsOperations, times(0)).getTrackPoint(any(), any(), any(), any());
		assertThat(act).isNull();
	}


}
