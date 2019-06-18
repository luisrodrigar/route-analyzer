package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.TCXService;
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
import static org.mockito.Mockito.doThrow;
import static utils.TestUtils.toRuntimeException;

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
        tcxXmlString = new String(TestUtils.getFileBytes(tcxXmlResource), StandardCharsets.UTF_8);
        String jsonActivityTcxStr = new String(TestUtils.getFileBytes(activityTcxResource), StandardCharsets.UTF_8);
        activityTcxTest = JsonUtils.fromJson(jsonActivityTcxStr, Activity.class);
    }

    @Test
    public void export() {
        // Given
        // When
        Try<String> result = Try.of(() -> tcxExportService.export(activityTcxTest));
        // Then
        result.onSuccess(tcxExportedFile ->assertThat(tcxExportedFile).isEqualTo(tcxXmlString))
                .onFailure(error -> assertThat(true).isFalse());
    }

    @Test
    public void exportActivityNull() {
        // When
        Try.of(() -> tcxExportService.export(null))
                // Then
                .onSuccess(tcxExportedFile -> assertThat(tcxExportedFile).isNull())
                .onFailure(error -> assertThat(true).isFalse());
    }

    @Test
    public void exportActivityThrowRuntimeException() {
        // Given
        Exception jaxbException = new JAXBException("Problems with xml.");
        // When
        doThrow(toRuntimeException(jaxbException)).when(tcxService).createXML(any());
        Try.of(() -> tcxExportService.export(activityTcxTest))
                // Then
                .onSuccess(tcxExportedFile -> assertThat(true).isFalse())
                .onFailure(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    RuntimeException runtimeException = (RuntimeException) error;
                    assertThat(runtimeException.getCause()).isInstanceOf(JAXBException.class);
                    assertThat(runtimeException.getCause().getMessage())
                            .isEqualTo("Problems with xml.");
                });
    }


}