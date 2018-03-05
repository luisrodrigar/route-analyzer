package com.routeanalyzer.database.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.routeanalyzer.model.TrackPoint;

public class TracksDocument {

	public static List<Document> toMongoDocumentsObject(List<TrackPoint> tracks) {
		List<Document> documents = new ArrayList<Document>();
		for(TrackPoint tP : tracks){
			Document trackDocument = new Document();
			trackDocument.append("time", tP.getDate())
					.append("position", 
							new Document("latitudeDegrees",tP.getPosition().getLatitudeDegrees().toString())
								.append("longitudeDegrees", tP.getPosition().getLongitudeDegrees().toString())
							)
					.append("altitudeMeters",tP.getAltitudeMeters().toString())
					.append("distanceMeters", tP.getDistanceMeters()!=null?tP.getDistanceMeters().toString():null)
					.append("heartRateBpm", tP.getHeartRateBpm())
					.append("speed", tP.getSpeed()!=null?tP.getSpeed().toString():null);
			documents.add(trackDocument);
		}
		return documents;
	}

}
