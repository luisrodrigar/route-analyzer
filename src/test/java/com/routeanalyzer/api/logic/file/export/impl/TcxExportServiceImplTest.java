package com.routeanalyzer.logic.file.export.impl;

import com.google.gson.Gson;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.common.TestUtils;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
public class TcxExportServiceImplTest {

    @Spy
    private TCXService tcxService;

    @InjectMocks
    private TcxExportFileService tcxExportService;

    @Value("classpath:utils/json-activity-tcx.json")
    private Resource activityTcxResource;
    @Value("classpath:utils/tcx-test.xml")
    private Resource tcxXmlResource;

    private Activity activityTcxTest;
    private String tcxXmlString;

    @Before
    public void setUp() {
        Gson gson = CommonUtils.getGsonLocalDateTime();
        tcxXmlString = new String(TestUtils.getFileBytes(tcxXmlResource), StandardCharsets.UTF_8);
        String jsonActivityTcxStr = new String(TestUtils.getFileBytes(activityTcxResource), StandardCharsets.UTF_8);
        activityTcxTest = gson.fromJson(jsonActivityTcxStr, Activity.class);

    }

    @Test
    public void export() {
        Try.run(() -> {
            String tcxExportedFile = tcxExportService.export(activityTcxTest);
            assertEquals(tcxXmlString, tcxExportedFile);
        }).onFailure((error) -> assertFalse(true));
    }
}