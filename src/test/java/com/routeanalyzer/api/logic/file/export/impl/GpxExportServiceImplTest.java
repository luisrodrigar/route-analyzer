package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.TestUtils;
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

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;

import static com.routeanalyzer.api.common.JsonUtils.fromJson;
import static com.routeanalyzer.api.common.TestUtils.toRuntimeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        activityGpxTest = fromJson(jsonActivityGpxStr, Activity.class);
    }

    @Test
    public void export() {
        // Given
        // When
        Try result = Try.of(() -> gpxExportService.export(activityGpxTest));
        // Then
        result.onSuccess(gpxExportedFile -> assertThat(gpxExportedFile).isEqualTo(gpxXmlString))
                .onFailure(error -> assertThat(true).isFalse());
    }

    @Test
    public void exportNullActivity() {
        // When
        Try.of(() -> gpxExportService.export(null))
                .onSuccess(gpxExportedFile -> assertThat(gpxExportedFile).isEmpty())
                .onFailure(error -> assertThat(true).isFalse());
    }

    @Test
    public void exportThrowJAXBException() {
        // Given
        Exception jaxbException = new JAXBException("Problems with the xml");
        // When
        doThrow(toRuntimeException(jaxbException)).when(gpxService).createXML(any());
        Try.of(() -> gpxExportService.export(activityGpxTest))
                .onSuccess(gpxExportedFile -> assertThat(true).isFalse())
                .onFailure(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    RuntimeException runtimeException = (RuntimeException) error;
                    assertThat(runtimeException.getCause()).isInstanceOf(JAXBException.class);
                    assertThat(runtimeException.getCause().getMessage()).isEqualTo("Problems with the xml");
                });
    }
}