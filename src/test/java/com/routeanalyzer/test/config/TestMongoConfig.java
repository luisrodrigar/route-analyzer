package com.routeanalyzer.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.routeanalyzer.database.ActivityMongoRepository;

@Profile("test")
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
		return "com.routeanalyzer.database";
	}
	
	@Bean
	@Override
	public MongoClient mongoClient() {
		return new Fongo("routeanalyzer-test").getMongo();
	}
	
}
