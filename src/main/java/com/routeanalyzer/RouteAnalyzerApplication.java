package com.routeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.routeanalyzer.config.ApplicationContextProvider;

@SpringBootApplication
public class RouteAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteAnalyzerApplication.class, args);
    }

    @Bean
    public CommonsMultipartResolver commonsMultipartResolver() {
        final CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(268435456); //256MB

        return commonsMultipartResolver;
    }
    
    @Bean
    public ApplicationContextProvider applicationContextProvider(){
    	return new ApplicationContextProvider();
    }
    
}