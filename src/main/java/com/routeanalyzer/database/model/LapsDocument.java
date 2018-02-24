package com.routeanalyzer.database.model;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.bson.Document;

import com.routeanalyzer.model.Lap;

public class LapsDocument {

	public static Set<Document> toMongoDocumentsObject(SortedSet<Lap> laps) {
		Set<Document> documents = new HashSet<Document>();
		for(Lap lap: laps){
			Document lapDocument = new Document();
			lapDocument.append("startTime", lap.getStartTime())
				.append("intensity", lap.getIntensity())
				.append("calories", lap.getCalories())
				.append("distanceMeters", lap.getDistanceMeters())
				.append("maximunHeartRate", lap.getMaximunHeartRate())
				.append("averageHearRate", lap.getAverageHearRate())
				.append("maximunSpeed", lap.getMaximunSpeed())
				.append("averageSpeed", lap.getAverageSpeed())
				.append("totalTimeSeconds", lap.getTotalTimeSeconds())
				.append("triggerMethod", lap.getTriggerMethod())
				.append("tracks", TracksDocument.toMongoDocumentsObject(lap.getTracks()));
			documents.add(lapDocument);
		}
		return documents;
	}

}
