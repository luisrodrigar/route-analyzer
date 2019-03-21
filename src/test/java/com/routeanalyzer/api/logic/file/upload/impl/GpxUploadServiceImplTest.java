package com.routeanalyzer.logic.file.upload.impl;

import com.google.gson.Gson;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.ActivityUtils;
import com.routeanalyzer.api.logic.LapsUtils;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.common.TestUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
public class GpxUploadServiceImplTest {

    @Spy
    private GPXService gpxService;

    @Mock
    private LapsUtils lapsUtils;

    @Mock
    private ActivityUtils activityUtils;

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
        Gson gson = CommonUtils.getGsonLocalDateTime();
        gpxObject = new GPXService().readXML(gpxXmlResource.getInputStream());
        String jsonActivityGpxStr = new String(TestUtils.getFileBytes(activityGpxResource), StandardCharsets.UTF_8);
        activityGpxTest = gson.fromJson(jsonActivityGpxStr, Activity.class);
    }

    @Test
    public void uploadTest() throws SAXParseException, JAXBException, IOException {
        doReturn(gpxObject).when(gpxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        assertEquals(Arrays.asList(activityGpxTest), gpxUploadService.upload(multipart));
    }

    @Test
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        doThrow(new JAXBException("")).when(gpxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        Try.of(() -> gpxUploadService.upload(multipart))
                .onSuccess((success) -> assertTrue(false))
                .onFailure((error) -> assertTrue(error instanceof JAXBException));
    }

}