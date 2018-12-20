package com.routeanalyzer.test.config;

import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.services.OriginalRouteAS3Service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Configuration
@Profile("test")
@TestPropertySource(locations="classpath:test.properties")
public class TestConfiguration{
	
	@SpyBean
	public ActivityUtils activityUtils;

	@MockBean
	public OriginalRouteAS3Service aS3Service;
	
	// Disabled the mongo db driver logger
	static Logger mongodbLogger = (Logger) LoggerFactory
	        .getLogger("org.mongodb.driver.cluster");

	static {
		mongodbLogger.setLevel(Level.OFF);
	}
	
}
