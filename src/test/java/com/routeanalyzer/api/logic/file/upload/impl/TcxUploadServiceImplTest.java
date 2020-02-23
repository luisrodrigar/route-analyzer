package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.TCXService;
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
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
public class TcxUploadServiceImplTest {

    @Spy
    private TCXService tcxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private ActivityOperations activityOperations;

    @InjectMocks
    private TcxUploadFileService tcxUploadService;

    @Value("classpath:utils/upload-file-tcx-test.json")
    private Resource activityTcxResource;
    @Value("classpath:utils/tcx-test.xml")
    private Resource tcxXmlResource;

    private Activity activityTcxTest;
    private TrainingCenterDatabaseT tcxObject;

    @Before
    public void setUp() throws Exception {
        String jsonActivityTcxStr = new String(TestUtils.getFileBytes(activityTcxResource), StandardCharsets.UTF_8);
        activityTcxTest = JsonUtils.fromJson(jsonActivityTcxStr, Activity.class)
                .getOrNull();
        tcxObject = tcxService.readXML(tcxXmlResource.getInputStream()).getOrNull();
    }

    @Test
    public void uploadTcxTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        // When
        doReturn(Try.success(tcxObject)).when(tcxService).readXML(Mockito.any());
        TrainingCenterDatabaseT result = tcxUploadService.upload(multipart).get();
        // Then
        assertThat(result).isEqualTo(tcxObject);
    }

    @Test(expected = JAXBException.class)
    public void uploadThrowExceptionTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        Exception jaxbException = new JAXBException("Error parser");
        // When
        doReturn(Try.failure(jaxbException)).when(tcxService).readXML(Mockito.any());
        // Then
        tcxUploadService.upload(multipart).get();
    }

    @Test
    public void xmlToModelTest() throws IOException {
        // Given

        // When
        List<Activity> result = tcxUploadService.toListActivities(tcxObject);

        // Then
        assertThat(result).isEqualTo(Arrays.asList(activityTcxTest));
    }

}
