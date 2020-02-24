package com.routeanalyzer.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lap implements Comparable<Lap>, Serializable {

	private static final long serialVersionUID = 1L;

	private ZonedDateTime startTime;
	// Field in cases when startTime field is not informed.
	private Integer index;
	private Double totalTimeSeconds;
	private Double distanceMeters;
	private Double maximumSpeed;
	private Double averageSpeed;
	private Double averageHearRate;
	private Integer maximumHeartRate, calories;
	private String intensity, triggerMethod, color, lightColor;
	@Builder.Default
	private List<TrackPoint> tracks = Lists.newArrayList();

	public Lap(ZonedDateTime startTime, Double totalTimeSeconds, Double distanceMeters, Double maximumSpeed,
			   Integer calories, Double averageSpeed, Double averageHearRate, Integer maximumHeartRate,
			   String intensity, String triggerMethod) {
		super();
		this.startTime = startTime;
		this.totalTimeSeconds = totalTimeSeconds;
		this.distanceMeters = distanceMeters;
		this.maximumSpeed = maximumSpeed;
		this.calories = calories;
		this.averageSpeed = averageSpeed;
		this.averageHearRate = averageHearRate;
		this.maximumHeartRate = maximumHeartRate;
		this.intensity = intensity;
		this.triggerMethod = triggerMethod;
	}

	public boolean addTrack(TrackPoint track){
		int sizeBeforeAdding = this.tracks.size();
		this.tracks.add(track);
		return this.tracks.size() == sizeBeforeAdding+1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Lap lap = (Lap) o;
		return Objects.equals(startTime, lap.startTime) &&
				index.equals(lap.index) &&
				Objects.equals(tracks, lap.tracks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startTime, index, tracks);
	}

	@Override
	public int compareTo(Lap o) {
		if(startTime!=null && o.getStartTime()!=null)
			return getStartTime().compareTo(o.getStartTime());
		else
			return getIndex().compareTo(o.getIndex());
	}
}
