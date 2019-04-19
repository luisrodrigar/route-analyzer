package com.routeanalyzer.api.database;

import com.routeanalyzer.api.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityMongoRepository extends MongoRepository<Activity, String> {
}
