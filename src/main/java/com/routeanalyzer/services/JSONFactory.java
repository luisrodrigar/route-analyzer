package com.routeanalyzer.services;

public class JSONFactory {

	private static JSONFactory instance;
	
	public static JSONFactory getInstance(){
		if(instance==null)
			instance = new JSONFactory();
		return instance;
	}
	
	private JSONFactory(){}
	
	public String xml2Json(){
		
		return null;
	}
	
}
