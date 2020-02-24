package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import io.vavr.control.Try;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.getFileBytes;
import static utils.TestUtils.toActivity;

@RunWith(MockitoJUnitRunner.class)
public class GpxExportServiceImplTest {

    @Spy
    private GPXService gpxService;

    @InjectMocks
    private GpxExportFileService gpxExportService;

    private static Activity activityGpxTest;
    private static String gpxXmlString;

    @BeforeClass
    public static void setUp() {
        activityGpxTest = toActivity("utils/upload-file-gpx-test.json");
        gpxXmlString = new String(getFileBytes("utils/gpx-test.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void export() {
        // Given

        // When
        Try<String> result = Try.of(() -> gpxExportService.export(activityGpxTest));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(gpxXmlString);
        verify(gpxService).createXML(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exportNullActivity() {
        // Given

        // When
        gpxExportService.export(null);

        // Then
        verify(gpxService, never()).createXML(any());
    }

    @Test(expected = JAXBException.class)
    public void exportThrowJAXBException() {
        // Given
        Exception jaxbException = new JAXBException("Problems with the xml");
        doReturn(Try.failure(jaxbException)).when(gpxService).createXML(any());

        // When
        gpxExportService.export(activityGpxTest);
    }
}
