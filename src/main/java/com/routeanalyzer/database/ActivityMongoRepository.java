package com.routeanalyzer.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.routeanalyzer.model.Activity;

@Repository
public interface ActivityMongoRepository extends MongoRepository<Activity, String> {
}
