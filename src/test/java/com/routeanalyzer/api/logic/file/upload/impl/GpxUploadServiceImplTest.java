package com.routeanalyzer.api.logic.file.upload.impl;

import utils.TestUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.routeanalyzer.api.common.JsonUtils.fromJson;
import static utils.TestUtils.toRuntimeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
@RunWith(SpringJUnit4ClassRunner.class)
public class GpxUploadServiceImplTest {

    @Spy
    private GPXService gpxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private ActivityOperations activityOperations;

    @InjectMocks
    private GpxUploadFileService gpxUploadService;

    @Value("classpath:utils/gpx-test.xml")
    private Resource gpxXmlResource;
    @Value("classpath:utils/upload-file-gpx-test.json")
    private Resource activityGpxResource;


    private GpxType gpxObject;
    private Activity activityGpxTest;

    @Before
    public void setUp() throws Exception {
        gpxObject = gpxService.readXML(gpxXmlResource.getInputStream());
        String jsonActivityGpxStr = new String(TestUtils.getFileBytes(activityGpxResource), StandardCharsets.UTF_8);
        activityGpxTest = fromJson(jsonActivityGpxStr, Activity.class);
    }

    @Test
    public void uploadGpxTest() throws JAXBException, IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        // When
        doReturn(gpxObject).when(gpxService).readXML(Mockito.any());
        List<Activity> result = gpxUploadService.upload(multipart);
        // Then
        assertThat(result).isEqualTo(Arrays.asList(activityGpxTest));
    }

    @Test
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        Exception jaxbException = new JAXBException("Problems with xml.");
        // When
        doThrow(toRuntimeException(jaxbException)).when(gpxService).readXML(Mockito.any());
        Try<List<Activity>> result = Try.of(() -> gpxUploadService.upload(multipart));
        // Then
        result.onSuccess((success) -> assertThat(true).isTrue())
                .onFailure(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    RuntimeException runExc = (RuntimeException) error;
                    assertThat(runExc.getCause()).isInstanceOf(JAXBException.class);
                });
    }

}