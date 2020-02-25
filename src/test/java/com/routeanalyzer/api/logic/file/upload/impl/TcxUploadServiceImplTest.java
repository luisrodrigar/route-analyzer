package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
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
import static utils.TestUtils.createValidTrainingCenterType;
import static utils.TestUtils.getStreamResource;
import static utils.TestUtils.toActivity;
import static com.routeanalyzer.api.common.MathUtils.round;

@RunWith(MockitoJUnitRunner.class)
public class TcxUploadServiceImplTest {

    @Mock
    private TCXService tcxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private ActivityOperations activityOperations;

    @Mock
    private TrackPointOperations trackPointOperations;

    @InjectMocks
    private TcxUploadFileService tcxUploadService;

    private static Activity activityTcxTest;
    private static TrainingCenterDatabaseT tcxObject;
    private InputStream tcxXmlInputStream;

    @BeforeClass
    public static void setUp() {
        tcxObject = createValidTrainingCenterType();
        activityTcxTest = toActivity("utils/upload-file-tcx-test.json");
    }

    @Before
    public void setInputStream() {
        tcxXmlInputStream = getStreamResource("utils/tcx-test.xml");
    }

    @Test
    public void uploadTcxTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlInputStream);

        // When
        doReturn(success(tcxObject)).when(tcxService).readXML(Mockito.any());
        Try<TrainingCenterDatabaseT> result = tcxUploadService.upload(multipart);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(tcxObject);
        verify(tcxService).readXML(any(InputStream.class));
    }

    @Test(expected = JAXBException.class)
    public void uploadThrowExceptionTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlInputStream);
        Exception jaxbException = new JAXBException("Error parser");
        doReturn(failure(jaxbException)).when(tcxService).readXML(Mockito.any());

        // When
        tcxUploadService.upload(multipart).get();

        // Then
        verify(tcxService).readXML(eq(tcxXmlInputStream));
    }

    @Test
    public void xmlToModelTest() {
        // Given
        doReturn(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 13), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.613197"))
                        .longitudeDegrees(new BigDecimal("-6.573217")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .distanceMeters(new BigDecimal("0.0"))
                .heartRateBpm(new Integer(96))
                .build()).when(trackPointOperations).toTrackPoint(any(TrackpointT.class), eq(1));
        doReturn(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 18), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.613217"))
                        .longitudeDegrees(new BigDecimal("-6.573373")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .distanceMeters(new BigDecimal("12.972326303345616"))
                .heartRateBpm(new Integer(96))
                .build()).when(trackPointOperations).toTrackPoint(any(TrackpointT.class), eq(2));
        doReturn(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 16, 20), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.613212"))
                        .longitudeDegrees(new BigDecimal("-6.573443")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .distanceMeters(new BigDecimal("18.734670396302985"))
                .heartRateBpm(new Integer(96))
                .build()).when(trackPointOperations).toTrackPoint(any(TrackpointT.class), eq(3));
        doReturn(TrackPoint.builder()
                .date(ZonedDateTime.of(LocalDateTime.of(2018, 2, 27, 13, 17, 30), ZoneId.of("UTC")))
                .position(Position.builder()
                        .latitudeDegrees(new BigDecimal("42.613212"))
                        .longitudeDegrees(new BigDecimal("-6.573825")).build())
                .altitudeMeters(new BigDecimal("557.3"))
                .distanceMeters(new BigDecimal("106.89949587498064"))
                .heartRateBpm(new Integer(120))
                .build()).when(trackPointOperations).toTrackPoint(any(TrackpointT.class), eq(4));

        // When
        List<Activity> result = tcxUploadService.toListActivities(tcxObject);

        // Then
        assertThat(result).isEqualTo(asList(activityTcxTest));
        assertThat(result.get(0).getLaps().get(0).getTracks().get(0).getSpeed().doubleValue())
                .isEqualTo(0.0);
        assertThat(round(result.get(0).getLaps().get(0).getTracks().get(1).getSpeed().doubleValue(), 15))
                .isEqualTo(2.594465260669123);
        assertThat(round(result.get(0).getLaps().get(0).getTracks().get(2).getSpeed().doubleValue(), 16))
                .isEqualTo(2.8811720464786834);
        assertThat(round(result.get(0).getLaps().get(1).getTracks().get(0).getSpeed().doubleValue(), 15))
                .isEqualTo(6.717731656189976);
        verify(activityOperations).calculateDistanceSpeedValues(any(Activity.class));
        verify(lapsOperations, times(2)).calculateLapValues(any(Lap.class));
        verify(trackPointOperations, times(4)).toTrackPoint(any(TrackpointT.class), any(Integer.class));
    }

}
