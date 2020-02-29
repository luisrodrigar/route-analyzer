package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static io.vavr.control.Try.failure;
import static io.vavr.control.Try.success;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.createValidGpxType;
import static utils.TestUtils.getStreamResource;
import static utils.TestUtils.toActivity;
import static utils.TestUtils.toGpxRootModel;
import static java.util.Optional.of;

@RunWith(MockitoJUnitRunner.class)
public class GpxUploadServiceImplTest {

    @Mock
    private GPXService gpxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private TrackPointOperations trackPointOperations;

    @Mock
    private ActivityOperations activityOperations;

    @InjectMocks
    private GpxUploadFileService gpxUploadService;

    private static GpxType gpxObject;
    private static Activity activityGpxTest;
    private InputStream gpxXmlInputStream;
    private static GpxType gpxType;

    @BeforeClass
    public static void setUp() {
        gpxObject = createValidGpxType();
        activityGpxTest = toActivity("expected/file/upload-file-gpx-test.json");
        gpxType = toGpxRootModel("input/gpx-test.xml");
    }

    @Before
    public void setUpInputStream() {
        gpxXmlInputStream = getStreamResource("input/gpx-test.xml");
    }

    @Test
    public void uploadGpxTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlInputStream);
        doReturn(success(gpxObject)).when(gpxService).readXML(Mockito.any());

        // When
        Try<GpxType> result = gpxUploadService.upload(multipart);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(gpxObject);
        verify(gpxService).readXML(any(InputStream.class));
    }

    @Test(expected = JAXBException.class)
    public void uploadThrowExceptionTest() throws Exception {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlInputStream);
        Exception jaxbException = new JAXBException("Problems with xml.");
        doReturn(failure(jaxbException)).when(gpxService).readXML(Mockito.any());

        // When
        gpxUploadService.upload(multipart).get();

        // Then
        verify(gpxService).readXML(eq(gpxXmlInputStream));
    }

    @Test
    public void xmlConvertToModelTest() {
        // Given
        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 13), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6131970"))
                        .longitudeDegrees(new BigDecimal("-6.5732170")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(1));
        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 18), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6132170"))
                        .longitudeDegrees(new BigDecimal("-6.5733730")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(2));
        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 20), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6132120"))
                        .longitudeDegrees(new BigDecimal("-6.5734430")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(3));

        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 30), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6132120"))
                        .longitudeDegrees(new BigDecimal("-6.5738250")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(4));
        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 33), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6132120"))
                        .longitudeDegrees(new BigDecimal("-6.5739120")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(5));

        doReturn(of(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 17, 30), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.6132120"))
                        .longitudeDegrees(new BigDecimal("-6.5738250")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .build())).when(trackPointOperations).toTrackPoint(any(WptType.class), eq(6));

        // When
        List<Activity> activities = gpxUploadService.toListActivities(gpxObject);

        // Then
        assertThat(activities).isEqualTo(asList(activityGpxTest));
        assertThat(activities.get(0).getLaps().get(0).getTracks().get(0).getHeartRateBpm())
                .isEqualTo(96);
        assertThat(activities.get(0).getLaps().get(0).getTracks().get(1).getHeartRateBpm())
                .isEqualTo(96);
        assertThat(activities.get(0).getLaps().get(0).getTracks().get(2).getHeartRateBpm())
                .isEqualTo(96);
        assertThat(activities.get(0).getLaps().get(1).getTracks().get(0).getHeartRateBpm())
                .isEqualTo(106);
        assertThat(activities.get(0).getLaps().get(1).getTracks().get(1).getHeartRateBpm())
                .isEqualTo(109);
        assertThat(activities.get(0).getLaps().get(2).getTracks().get(0).getHeartRateBpm())
                .isEqualTo(120);
        verify(activityOperations).calculateDistanceSpeedValues(any(Activity.class));
        verify(lapsOperations, times(3)).calculateLapValues(any(Lap.class));
        verify(trackPointOperations, times(6)).toTrackPoint(any(WptType.class), any(Integer.class));
    }

}
