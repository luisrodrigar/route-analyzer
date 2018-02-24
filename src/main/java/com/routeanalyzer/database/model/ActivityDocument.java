package com.routeanalyzer.database.model;

import org.bson.Document;

import com.routeanalyzer.model.Activity;

public class ActivityDocument {
	
	public static Document toMongoDocumentObject(Activity activity){
		return new Document("name", activity.getName())
				.append("sport", activity.getSport())
				.append("date", activity.getDate())
				.append("idUser", activity.getIdUser())
				.append("device", activity.getDevice())
				.append("laps", LapsDocument.toMongoDocumentsObject(activity.getLaps()));
				
	}
	
}
