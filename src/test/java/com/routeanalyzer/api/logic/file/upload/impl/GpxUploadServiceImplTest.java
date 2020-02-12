package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.JsonUtils;
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
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;

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
        gpxObject = gpxService.readXML(gpxXmlResource.getInputStream()).get();
        String jsonActivityGpxStr = new String(TestUtils.getFileBytes(activityGpxResource), StandardCharsets.UTF_8);
        activityGpxTest = JsonUtils.fromJson(jsonActivityGpxStr, Activity.class)
                .getOrNull();
    }

    @Test
    public void uploadGpxTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        // When
        doReturn(Try.success(gpxObject)).when(gpxService).readXML(Mockito.any());
        GpxType result = gpxUploadService.upload(multipart).get();
        // Then
        assertThat(result).isEqualTo(gpxObject);
    }

    @Test(expected = JAXBException.class)
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        Exception jaxbException = new JAXBException("Problems with xml.");
        // When
        // Then
        doReturn(Try.failure(jaxbException)).when(gpxService).readXML(Mockito.any());
        gpxUploadService.upload(multipart).get();
    }

    @Test
    public void xmlConvertToModelTest() {
        // Given

        // When
        List<Activity> activities = gpxUploadService.toListActivities(gpxObject);

        // Then
        assertThat(activities).isEqualTo(asList(activityGpxTest));
    }

}
