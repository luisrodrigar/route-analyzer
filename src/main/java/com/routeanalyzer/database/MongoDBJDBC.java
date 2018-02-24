package com.routeanalyzer.database;

import org.bson.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.routeanalyzer.config.ApplicationContextProvider;
import com.routeanalyzer.database.dao.ActivityDAO;

public class MongoDBJDBC {

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private MongoTemplate mongoTemplate;
	
	protected MongoDBJDBC(){}
	
	public static MongoClient getInstance(){
		if(mongoClient==null)
			mongoClient = new MongoClient("localhost",27017);
		return mongoClient;
	}
	
	public MongoDatabase getDataBase(){
		if(database==null)
			database = getInstance().getDatabase("routeanalyzer");
		return database;
	}
	
	public void createDBCollection(String collectionName){
		getDataBase().createCollection(collectionName);
	}
	
	public MongoCollection<Document> getDBCollection(String collectionName){
		return getDataBase().getCollection(collectionName);
	}
	
	public MongoCollection<Document> getDBActivitiesCollection(){
		return getDataBase().getCollection("activities");
	}

	public MongoTemplate getMongoTemplate(){
		return mongoTemplate;
	}
	
	public void setMongoTemplate(MongoTemplate mongoTemplate){
		this.mongoTemplate = mongoTemplate;
	}
	
	public ActivityDAO getActivityDAOImpl(){
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();	
		return ctxt.getBean("activityDAO", ActivityDAO.class);
	}
	
}
