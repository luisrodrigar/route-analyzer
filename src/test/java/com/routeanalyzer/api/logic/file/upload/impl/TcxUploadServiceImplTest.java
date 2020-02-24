package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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
import static utils.TestUtils.createValidTrainingCenterType;
import static utils.TestUtils.getStreamResource;
import static utils.TestUtils.toActivity;

@RunWith(MockitoJUnitRunner.class)
public class TcxUploadServiceImplTest {

    @Spy
    private TCXService tcxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private ActivityOperations activityOperations;

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

        // When
        List<Activity> result = tcxUploadService.toListActivities(tcxObject);

        // Then
        assertThat(result).isEqualTo(asList(activityTcxTest));
        verify(activityOperations).calculateDistanceSpeedValues(any(Activity.class));
        verify(lapsOperations, times(2)).calculateLapValues(any(Lap.class));
    }

}
