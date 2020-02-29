package com.routeanalyzer.api.facade.impl;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.FileNotFoundException;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import io.vavr.control.Try;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import utils.TestUtils;

import java.util.List;

import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;

@RunWith(MockitoJUnitRunner.class)
public class FileFacadeImplTest {

    @InjectMocks
    private FileFacadeImpl fileFacade;

    @Mock
    private TcxUploadFileService tcxService;
    @Mock
    private GpxUploadFileService gpxService;
    @Mock
    private ActivityOperations activityOperations;
    @Mock
    private ActivityMongoRepository mongoRepository;

    @Test
    public void uploadGpxFile() {
        // Given
        MultipartFile multipartFile = new MockMultipartFile("file", "",
                APPLICATION_XML_VALUE, TestUtils.getFileBytes("input/coruna.gpx.xml"));
        Activity activity = TestUtils.toActivity("expected/file/upload-file-gpx-test.json");
        activity.setId(ACTIVITY_GPX_ID);
        List<Activity> activities = asList(activity);
        doReturn(of(activities)).when(activityOperations).upload(multipartFile, gpxService);
        doReturn(activities).when(mongoRepository).saveAll(activities);
        doReturn(activities).when(activityOperations).pushToS3(activities, multipartFile);


        // When
        Try<List<String>> tryResult = Try.of(() -> fileFacade.uploadFile(multipartFile, SOURCE_GPX_XML));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(asList(ACTIVITY_GPX_ID));
        verify(activityOperations).upload(eq(multipartFile), eq(gpxService));
        verify(mongoRepository).saveAll(eq(activities));
        verify(activityOperations).pushToS3(eq(activities), eq(multipartFile));
    }

    @Test
    public void uploadTcxFile() {
        // Given
        MultipartFile multipartFile = new MockMultipartFile("file", "",
                APPLICATION_XML_VALUE, TestUtils.getFileBytes("input/oviedo.tcx.xml"));
        Activity activity = TestUtils.toActivity("expected/file/upload-file-tcx-test.json");
        activity.setId(ACTIVITY_TCX_ID);
        List<Activity> activities = asList(activity);
        doReturn(of(activities)).when(activityOperations).upload(multipartFile, tcxService);
        doReturn(activities).when(mongoRepository).saveAll(activities);
        doReturn(activities).when(activityOperations).pushToS3(activities, multipartFile);

        // When
        Try<List<String>> tryResult = Try.of(() -> fileFacade.uploadFile(multipartFile, SOURCE_TCX_XML));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(asList(ACTIVITY_TCX_ID));
        verify(activityOperations).upload(eq(multipartFile), eq(tcxService));
        verify(mongoRepository).saveAll(eq(activities));
        verify(activityOperations).pushToS3(eq(activities), eq(multipartFile));
    }

    @Test(expected = FileOperationNotExecutedException.class)
    public void uploadGpxErrorHappenedProcessingFile() throws FileOperationNotExecutedException {
        // Given
        MultipartFile multipartFile = new MockMultipartFile("file", "",
                APPLICATION_XML_VALUE, TestUtils.getFileBytes("input/oviedo.tcx.xml"));
        doReturn(empty()).when(activityOperations).upload(multipartFile, tcxService);

        // When
        fileFacade.uploadFile(multipartFile, SOURCE_TCX_XML);

        // Then
        verify(activityOperations).upload(eq(multipartFile), eq(tcxService));
        verify(mongoRepository, never()).saveAll(any());
        verify(activityOperations, never()).pushToS3(any(), any());
    }

    @Test
    public void getFile() {
        // Given
        String file = "Downloaded file";
        doReturn(of(file)).when(activityOperations).getOriginalFile(ACTIVITY_TCX_ID, SOURCE_TCX_XML);

        // When
        Try<String> tryResult = Try.of(() -> fileFacade.getFile(ACTIVITY_TCX_ID, SOURCE_TCX_XML));

        // Then
        assertThat(tryResult.isSuccess()).isTrue();
        assertThat(tryResult.get()).isEqualTo(file);
        verify(activityOperations).getOriginalFile(eq(ACTIVITY_TCX_ID), eq(SOURCE_TCX_XML));
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileNotFound() throws FileNotFoundException {
        // Given
        doReturn(empty()).when(activityOperations).getOriginalFile(ACTIVITY_TCX_ID, SOURCE_TCX_XML);

        // When
        fileFacade.getFile(ACTIVITY_TCX_ID, SOURCE_TCX_XML);

        // Then
        verify(activityOperations).getOriginalFile(eq(ACTIVITY_TCX_ID), eq(SOURCE_TCX_XML));
    }
}
