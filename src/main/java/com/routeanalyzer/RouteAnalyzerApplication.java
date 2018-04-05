package com.routeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.routeanalyzer.database.ActivityMongoRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses=ActivityMongoRepository.class)
public class RouteAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteAnalyzerApplication.class, args);
    }
    
}