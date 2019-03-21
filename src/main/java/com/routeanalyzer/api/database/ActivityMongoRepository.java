package com.routeanalyzer.api.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.routeanalyzer.api.model.Activity;

@Repository
public interface ActivityMongoRepository extends MongoRepository<Activity, String> {
}
