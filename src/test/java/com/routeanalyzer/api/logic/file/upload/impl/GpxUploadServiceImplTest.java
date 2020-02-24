package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.GpxType;
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

@RunWith(MockitoJUnitRunner.class)
public class GpxUploadServiceImplTest {

    @Mock
    private GPXService gpxService;

    @Mock
    private LapsOperations lapsOperations;

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
        activityGpxTest = toActivity("utils/upload-file-gpx-test.json");
        gpxType = toGpxRootModel("utils/gpx-test.xml");
    }

    @Before
    public void setUpInputStream() {
        gpxXmlInputStream = getStreamResource("utils/gpx-test.xml");
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

        // When
        List<Activity> activities = gpxUploadService.toListActivities(gpxObject);

        // Then
        assertThat(activities).isEqualTo(asList(activityGpxTest));
        verify(activityOperations).calculateDistanceSpeedValues(any(Activity.class));
        verify(lapsOperations, times(3)).calculateLapValues(any(Lap.class));
    }

}
