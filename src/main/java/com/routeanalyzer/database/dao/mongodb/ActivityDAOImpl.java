package com.routeanalyzer.database.dao.mongodb;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.WriteResult;
import com.routeanalyzer.database.dao.ActivityDAO;
import com.routeanalyzer.model.Activity;

public class ActivityDAOImpl implements ActivityDAO {

	private MongoOperations mongoOps;
	private static final String ACTIVITY_COLLECTION = "activities";
	
	public ActivityDAOImpl(MongoOperations mongoOps) {
		this.mongoOps = mongoOps;
	}
	
	@Override
	public void create(Activity activity) {
		this.mongoOps.insert(activity, ACTIVITY_COLLECTION);
	}

	@Override
	public Activity readById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		return this.mongoOps.findOne(query, Activity.class, ACTIVITY_COLLECTION);
	}

	@Override
	public void update(Activity activity) {
		this.mongoOps.save(activity, ACTIVITY_COLLECTION);
	}

	@Override
	public int deleteById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		WriteResult result = this.mongoOps.remove(query, Activity.class, ACTIVITY_COLLECTION);
		return result.getN();
	}

}
