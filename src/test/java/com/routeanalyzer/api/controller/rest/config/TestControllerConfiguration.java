package com.routeanalyzer.api.controller.rest.config;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

import com.routeanalyzer.api.logic.ActivityUtils;
import com.routeanalyzer.api.services.OriginalRouteAS3Service;

@Configuration
@Profile("test-controller")
@TestPropertySource(locations="classpath:test.properties")
public class TestControllerConfiguration{
	
	@SpyBean
	public ActivityUtils activityUtils;

	@MockBean
	public OriginalRouteAS3Service aS3Service;
	
}
