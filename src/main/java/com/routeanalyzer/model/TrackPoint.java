package com.routeanalyzer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class TrackPoint implements Comparable<TrackPoint>, Serializable {

	private static final long serialVersionUID = 1L;

	private LocalDateTime date;
	// Field in cases when date field is not informed.
	private Integer index;
	private Position position;
	private BigDecimal altitudeMeters;
	private BigDecimal distanceMeters;
	private BigDecimal speed;
	private Integer heartRateBpm;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
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
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}

	@Override
	public int compareTo(TrackPoint o) {
		if (date != null && o.getDate() != null)
			return getDate().compareTo(o.getDate());
		else
			return getIndex().compareTo(o.getIndex());
	}

}
