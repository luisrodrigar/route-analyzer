package com.routeanalyzer.test.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.services.OriginalRouteAS3Service;

@Profile("test")
@Configuration
public class TestConfiguration{
	
	@MockBean
	public ActivityUtils activityUtils;

	@MockBean
	public OriginalRouteAS3Service originalRouteAS3Service;
	
}
