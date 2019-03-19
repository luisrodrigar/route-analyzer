package com.routeanalyzer.model;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Lap implements Comparable<Lap>, Serializable {

	private static final long serialVersionUID = 1L;

	private LocalDateTime startTime;
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

	public Lap(LocalDateTime startTime, Double totalTimeSeconds, Double distanceMeters, Double maximumSpeed,
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lap other = (Lap) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}
	@Override
	public int compareTo(Lap o) {
		if(startTime!=null && o.getStartTime()!=null)
			return getStartTime().compareTo(o.getStartTime());
		else
			return getIndex().compareTo(o.getIndex());
	}
}
