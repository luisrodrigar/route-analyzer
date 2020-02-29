package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.TCXService;
import io.vavr.control.Try;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;

import static io.vavr.control.Try.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.getFileBytes;
import static utils.TestUtils.toActivity;

@RunWith(MockitoJUnitRunner.class)
public class TcxExportServiceImplTest {

    @Mock
    private TCXService tcxService;

    @InjectMocks
    private TcxExportFileService tcxExportService;

    private static Activity activityTcxTest;
    private static String tcxXmlString;

    @BeforeClass
    public static void setUp() {
        activityTcxTest = toActivity("input/json-activity-tcx.json");
        tcxXmlString = new String(getFileBytes("expected/file/tcx-test.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void export() {
        // Given
        doReturn(success(tcxXmlString)).when(tcxService).createXML(any(JAXBElement.class));

        // When
        Try<String> result = Try.of(() -> tcxExportService.export(activityTcxTest));

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(tcxXmlString);
        verify(tcxService).createXML(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exportActivityNull() {
        // Given

        // When
        tcxExportService.export(null);

        // Then
        verify(tcxService, never()).createXML(any());
    }

    @Test(expected = JAXBException.class)
    public void exportActivityThrowRuntimeException() {
        // Given
        Exception jaxbException = new JAXBException("Problems with xml.");
        doReturn(Try.failure(jaxbException)).when(tcxService).createXML(any());

        // When
        tcxExportService.export(activityTcxTest);

        // Then
        verify(tcxService).createXML(any());
    }


}
