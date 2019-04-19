package com.routeanalyzer.api.controller.rest.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Profile("test-mongodb")
@Configuration
@ComponentScan(basePackageClasses = { ActivityMongoRepository.class })
@EnableMongoRepositories
public class TestMongoConfig extends AbstractMongoConfiguration {
	
	@Override
	protected String getDatabaseName() {
		return "routeanalyzer-test";
	}

	@Override
	protected String getMappingBasePackage() {
		return "com.routeanalyzer.api.database";
	}
	
	@Bean
	@Override
	public MongoClient mongoClient() {
		return new Fongo("routeanalyzer-test").getMongo();
	}
	
	// Disabled the mongo db driver logger
	static Logger mongodbLogger = (Logger) LoggerFactory
	        .getLogger("org.mongodb.driver.cluster");

	static {
		mongodbLogger.setLevel(Level.OFF);
	}
	
}