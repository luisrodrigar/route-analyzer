package com.routeanalyzer.logic.file.upload.impl;

import com.google.gson.Gson;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.ActivityUtils;
import com.routeanalyzer.api.logic.LapsUtils;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.common.TestUtils;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
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
public class TcxUploadServiceImplTest {

    @Spy
    private TCXService tcxService;

    @Mock
    private LapsUtils lapsUtils;

    @Mock
    private ActivityUtils activityUtils;

    @InjectMocks
    private TcxUploadFileService tcxUploadService;

    @Value("classpath:utils/json-activity-tcx.json")
    private Resource activityTcxResource;
    @Value("classpath:utils/tcx-test.xml")
    private Resource tcxXmlResource;

    private Activity activityTcxTest;
    private TrainingCenterDatabaseT tcxObject;

    @Before
    public void setUp() throws Exception {
        Gson gson = CommonUtils.getGsonLocalDateTime();
        String jsonActivityTcxStr = new String(TestUtils.getFileBytes(activityTcxResource), StandardCharsets.UTF_8);
        activityTcxTest = gson.fromJson(jsonActivityTcxStr, Activity.class);
        tcxObject = new TCXService().readXML(tcxXmlResource.getInputStream());
    }

    @Test
    public void uploadTest() throws SAXParseException, JAXBException, IOException {
        doReturn(tcxObject).when(tcxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        assertEquals(Arrays.asList(activityTcxTest), tcxUploadService.upload(multipart));
    }

    @Test
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        doThrow(new SAXParseException("", null)).when(tcxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        Try.of(() -> tcxUploadService.upload(multipart))
                .onSuccess((success) -> assertTrue(false))
                .onFailure((error) -> assertTrue(error instanceof SAXParseException));
    }

}