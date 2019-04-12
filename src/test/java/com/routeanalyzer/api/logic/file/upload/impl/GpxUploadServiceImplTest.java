package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.common.TestUtils;
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import static org.assertj.core.api.Assertions.assertThat;
import static com.routeanalyzer.api.common.JsonUtils.fromJson;

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
    @Value("classpath:utils/json-activity-gpx.json")
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
    public void uploadTest() throws JAXBException, IOException {
        doReturn(gpxObject).when(gpxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        assertThat(Arrays.asList(activityGpxTest)).isEqualTo(gpxUploadService.upload(multipart));
    }

    @Test
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        doThrow(new JAXBException("")).when(gpxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        Try.of(() -> gpxUploadService.upload(multipart))
                .onSuccess((success) -> assertThat(true).isTrue())
                .onFailure(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    RuntimeException runExc = RuntimeException.class.cast(error);
                    assertThat(runExc.getCause()).isInstanceOf(JAXBException.class);
                });
    }

}