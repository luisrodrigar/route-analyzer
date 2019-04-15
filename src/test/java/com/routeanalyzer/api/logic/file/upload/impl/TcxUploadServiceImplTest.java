package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.TestUtils;
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

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.routeanalyzer.api.common.JsonUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

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

    @Value("classpath:utils/json-activity-tcx.json")
    private Resource activityTcxResource;
    @Value("classpath:utils/tcx-test.xml")
    private Resource tcxXmlResource;

    private Activity activityTcxTest;
    private TrainingCenterDatabaseT tcxObject;

    @Before
    public void setUp() throws Exception {
        String jsonActivityTcxStr = new String(TestUtils.getFileBytes(activityTcxResource), StandardCharsets.UTF_8);
        activityTcxTest = fromJson(jsonActivityTcxStr, Activity.class);
        tcxObject = tcxService.readXML(tcxXmlResource.getInputStream());
    }

    @Test
    public void uploadTest() throws JAXBException, IOException {
        doReturn(tcxObject).when(tcxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        assertThat(Arrays.asList(activityTcxTest)).isEqualTo(tcxUploadService.upload(multipart));
    }

    @Test
    public void uploadThrowExceptionTest() throws IOException, JAXBException {
        doThrow(new JAXBException("")).when(tcxService).readXML(Mockito.any());
        MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
        Try.of(() -> tcxUploadService.upload(multipart))
                .onSuccess((success) -> assertThat(true).isTrue())
                .onFailure((error) -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    RuntimeException runExc = RuntimeException.class.cast(error);
                    assertThat(runExc.getCause()).isInstanceOf(JAXBException.class);
                });
    }

}