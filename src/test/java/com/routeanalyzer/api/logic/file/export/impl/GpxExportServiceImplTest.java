package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
public class GpxExportServiceImplTest {

    @Spy
    private GPXService gpxService;

    @InjectMocks
    private GpxExportFileService gpxExportService;

    @Value("classpath:utils/upload-file-gpx-test.json")
    private Resource activityGpxResource;
    @Value("classpath:utils/gpx-test.xml")
    private Resource gpxXmlResource;

    private Activity activityGpxTest;
    private String gpxXmlString;

    @Before
    public void setUp() {
        gpxXmlString = new String(TestUtils.getFileBytes(gpxXmlResource), StandardCharsets.UTF_8);
        String jsonActivityGpxStr = new String(TestUtils.getFileBytes(activityGpxResource), StandardCharsets.UTF_8);
        activityGpxTest = JsonUtils.fromJson(jsonActivityGpxStr, Activity.class).getOrNull();
    }

    @Test
    public void export() {
        // Given
        // When
        Try<String> result = Try.of(() -> gpxExportService.export(activityGpxTest));
        // Then
        result.onSuccess(gpxExportedFile -> assertThat(gpxExportedFile).isEqualTo(gpxXmlString))
                .onFailure(error -> assertThat(true).isFalse());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exportNullActivity() {
        // When
        gpxExportService.export(null);
    }

    @Test(expected = JAXBException.class)
    public void exportThrowJAXBException() {
        // Given
        Exception jaxbException = new JAXBException("Problems with the xml");
        // When
        doReturn(Try.failure(jaxbException)).when(gpxService).createXML(any());
        gpxExportService.export(activityGpxTest);
    }
}
