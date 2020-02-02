package com.routeanalyzer.api.controller.rest.config;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Profile("test-controller")
@TestPropertySource(locations="classpath:test.properties")
public class TestControllerConfiguration{
	
	@SpyBean
	public ActivityOperations activityOperations;

	@MockBean
	public OriginalActivityRepository aS3Service;

	@MockBean
	public TcxExportFileService tcxExportFileService;

	@MockBean
	public GpxExportFileService gpxExportFileService;

	@MockBean
	public TcxUploadFileService tcxUploadFileService;

	@MockBean
	public GpxUploadFileService gpxUploadFileService;
	
}
