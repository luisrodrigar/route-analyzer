package com.routeanalyzer.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TrackPoint implements Comparable<TrackPoint>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Date date;
	// Field in cases when date field is not informed.
	private Integer index;
	private Position position;
	private BigDecimal altitudeMeters, distanceMeters, speed;
	private Integer heartRateBpm;
	
	public TrackPoint(Date date, Integer index, Position position, BigDecimal altitudeMeters, BigDecimal distanceMeters, BigDecimal speed,
			Integer heartRateBpm) {
		super();
		this.date = date;
		this.index = index;
		this.position = position;
		this.altitudeMeters = altitudeMeters;
		this.distanceMeters = distanceMeters;
		this.speed = speed;
		this.heartRateBpm = heartRateBpm;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public BigDecimal getAltitudeMeters() {
		return altitudeMeters;
	}
	public void setAltitudeMeters(BigDecimal altitudeMeters) {
		this.altitudeMeters = altitudeMeters;
	}
	public BigDecimal getDistanceMeters() {
		return distanceMeters;
	}
	public void setDistanceMeters(BigDecimal distanceMeters) {
		this.distanceMeters = distanceMeters;
	}
	public BigDecimal getSpeed() {
		return speed;
	}
	public void setSpeed(BigDecimal speed) {
		this.speed = speed;
	}
	public Integer getHeartRateBpm() {
		return heartRateBpm;
	}
	public void setHeartRateBpm(Integer heartRateBpm) {
		this.heartRateBpm = heartRateBpm;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		TrackPoint other = (TrackPoint) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}
	@Override
	public int compareTo(TrackPoint o) {
		if(date!=null && o.getDate()!=null)
			return getDate().compareTo(o.getDate());
		else
			return getIndex().compareTo(o.getIndex());
	}
	@Override
	public String toString() {
		return "TrackPoint ["
				+ (date!=null ? "\n\tdate=" + date : "")
				+ (index!=null ? "\n\tindex trackpoint=" + index : "")
				+ (position!=null ? ",\n\tposition=" + position : "")
				+ (altitudeMeters!=null ? ",\n\taltitudeMeters=" + altitudeMeters : "")
				+ (distanceMeters!=null ? ",\n\tdistanceMeters=" + distanceMeters : "")
				+ (speed!=null ? ",\n\tspeed=" + speed : "")
				+ (heartRateBpm!=null ? ",\n\theartRateBpm=" + heartRateBpm : "")  
				+ "\n]";
	}
}
