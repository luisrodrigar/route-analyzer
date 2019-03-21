package com.routeanalyzer.api.logic.file.export.impl;

import com.google.gson.Gson;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.common.TestUtils;
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
public class GpxExportServiceImplTest {

    @Spy
    private GPXService gpxService;

    @InjectMocks
    private GpxExportFileService gpxExportService;

    @Value("classpath:utils/json-activity-gpx.json")
    private Resource activityGpxResource;
    @Value("classpath:utils/gpx-test.xml")
    private Resource gpxXmlResource;

    private Activity activityGpxTest;
    private String gpxXmlString;

    @Before
    public void setUp() {
        Gson gson = CommonUtils.getGsonLocalDateTime();
        gpxXmlString = new String(TestUtils.getFileBytes(gpxXmlResource), StandardCharsets.UTF_8);
        String jsonActivityGpxStr = new String(TestUtils.getFileBytes(activityGpxResource), StandardCharsets.UTF_8);
        activityGpxTest = gson.fromJson(jsonActivityGpxStr, Activity.class);
    }

    @Test
    public void export() {
        Try.run(() -> {
            String gpxExportedFile = gpxExportService.export(activityGpxTest);
            assertEquals(gpxXmlString, gpxExportedFile);
        }).onFailure((error) -> assertFalse(true));
    }
}