package com.routeanalyzer.model;

import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Lap implements Comparable<Lap> {
	
	private Date startTime;
	// Field in cases when startTime field is not informed.
	private Integer index;
	private Double totalTimeSeconds, distanceMeters, maximunSpeed, averageSpeed;
	private Integer averageHearRate, maximunHeartRate, calories;
	private String intensity, triggerMethod;
	private SortedSet<TrackPoint> tracks;
	
	public Lap(Date startTime, Double totalTimeSeconds, Double distanceMeters, Double maximunSpeed, Integer calories,
			Double averageSpeed, Integer averageHearRate, Integer maximunHeartRate, String intensity,
			String triggerMethod) {
		super();
		this.startTime = startTime;
		this.totalTimeSeconds = totalTimeSeconds;
		this.distanceMeters = distanceMeters;
		this.maximunSpeed = maximunSpeed;
		this.calories = calories;
		this.averageSpeed = averageSpeed;
		this.averageHearRate = averageHearRate;
		this.maximunHeartRate = maximunHeartRate;
		this.intensity = intensity;
		this.triggerMethod = triggerMethod;
		tracks = new TreeSet<TrackPoint>();
	}
	public Lap() {
		tracks = new TreeSet<TrackPoint>();
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Double getTotalTimeSeconds() {
		return totalTimeSeconds;
	}
	public void setTotalTimeSeconds(Double totalTimeSeconds) {
		this.totalTimeSeconds = totalTimeSeconds;
	}
	public Double getDistanceMeters() {
		return distanceMeters;
	}
	public void setDistanceMeters(Double distanceMeters) {
		this.distanceMeters = distanceMeters;
	}
	public Double getMaximunSpeed() {
		return maximunSpeed;
	}
	public void setMaximunSpeed(Double maximunSpeed) {
		this.maximunSpeed = maximunSpeed;
	}
	public Integer getCalories() {
		return calories;
	}
	public void setCalories(Integer calories) {
		this.calories = calories;
	}
	public Double getAverageSpeed() {
		return averageSpeed;
	}
	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}
	public Integer getAverageHearRate() {
		return averageHearRate;
	}
	public void setAverageHearRate(Integer averageHearRate) {
		this.averageHearRate = averageHearRate;
	}
	public Integer getMaximunHeartRate() {
		return maximunHeartRate;
	}
	public void setMaximunHeartRate(Integer maximunHeartRate) {
		this.maximunHeartRate = maximunHeartRate;
	}
	public String getIntensity() {
		return intensity;
	}
	public void setIntensity(String intensity) {
		this.intensity = intensity;
	}
	public String getTriggerMethod() {
		return triggerMethod;
	}
	public void setTriggerMethod(String triggerMethod) {
		this.triggerMethod = triggerMethod;
	}
	
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public SortedSet<TrackPoint> getTracks() {
		return Collections.unmodifiableSortedSet(tracks);
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
	@Override
	public String toString() {
		return "Lap ["
				+ (startTime!=null ? ("\n\tstartTime=" + startTime): "" ) 
				+ (index!=null ? ("\n\tindex lap=" + index) : "")
				+ (totalTimeSeconds!=null ? (",\n\ttotalTimeSeconds=" + totalTimeSeconds):"")
				+ (distanceMeters!=null ? (",\n\tdistanceMeters=" + distanceMeters ) : "")
				+ (maximunSpeed!=null? (",\n\tmaximunSpeed=" + maximunSpeed) :"")
				+ (averageSpeed!=null? (",\n\taverageSpeed=" + averageSpeed) :"")
				+ (averageHearRate!=null ? (",\n\taverageHearRate=" + averageHearRate) : "")
				+ (maximunHeartRate!=null ? (",\n\tmaximunHeartRate=" + maximunHeartRate) : "") 
				+ (calories!=null ? (",\n\tcalories=" + calories) : "")
				+ (intensity!=null ? (",\n\tintensity=" + intensity) : "")
				+ (triggerMethod!=null ? (",\n\ttriggerMethod=" + triggerMethod) : "")
				+ ( tracks!=null && !tracks.isEmpty() ? (",\n\ttracks=" + 
						(tracks.stream().map(Object::toString).collect(Collectors.joining(",\n\t"))) ) : "")
				+ "\n]";
	}
}
