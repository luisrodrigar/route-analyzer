package com.routeanalyzer.database.dao;

import com.routeanalyzer.model.Activity;

public interface ActivityDAO {

	public void create(Activity activity);
	
	public Activity readById(String id);
	
	public void update(Activity activity);
	
	public int deleteById(String id);
	
}
