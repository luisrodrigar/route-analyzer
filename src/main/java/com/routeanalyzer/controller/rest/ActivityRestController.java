package com.routeanalyzer.controller.rest;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.routeanalyzer.config.ApplicationContextProvider;
import com.routeanalyzer.database.MongoDBJDBC;
import com.routeanalyzer.database.dao.ActivityDAO;
import com.routeanalyzer.model.Activity;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("activity")
public class ActivityRestController {

	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> getActivityById(@PathVariable String id){
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);
		return act!=null ? new ResponseEntity<Activity>(act, HttpStatus.ACCEPTED) 
				: new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
}
