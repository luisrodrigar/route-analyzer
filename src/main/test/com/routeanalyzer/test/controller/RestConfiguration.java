package com.routeanalyzer.test.controller;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.services.OriginalRouteAS3Service;

@Profile("test")
@Configuration
public class RestConfiguration {
	@Bean
	@Primary
	public ActivityUtils activityUtil(){
		return Mockito.mock(ActivityUtils.class);
	}
	@Bean
	@Primary
	public OriginalRouteAS3Service originalRouteAS3Service(){
		return Mockito.mock(OriginalRouteAS3Service.class);
	}
	@Bean
	@Primary
	public ActivityMongoRepository activityMongoRepository(){
		return Mockito.mock(ActivityMongoRepository.class);
	}
}
