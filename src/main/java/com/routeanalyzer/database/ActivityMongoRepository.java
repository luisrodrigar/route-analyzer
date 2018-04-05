package com.routeanalyzer.database;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.routeanalyzer.model.Activity;

public interface ActivityMongoRepository extends MongoRepository<Activity, String> {
	
	
}
