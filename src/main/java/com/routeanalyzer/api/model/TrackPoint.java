package com.routeanalyzer.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackPoint implements Comparable<TrackPoint>, Serializable {

	private static final long serialVersionUID = 1L;

	private ZonedDateTime date;
	// Field in cases when date field is not informed.
	private Integer index;
	private Position position;
	private BigDecimal altitudeMeters;
	private BigDecimal distanceMeters;
	private BigDecimal speed;
	private Integer heartRateBpm;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackPoint that = (TrackPoint) o;
		return date.equals(that.date) &&
				position.equals(that.position);
	}

	@Override
	public int hashCode() {
		return Objects.hash(date, position);
	}

	@Override
	public int compareTo(TrackPoint o) {
		if (date != null && o.getDate() != null)
			return getDate().compareTo(o.getDate());
		else
			return getIndex().compareTo(o.getIndex());
	}

}
