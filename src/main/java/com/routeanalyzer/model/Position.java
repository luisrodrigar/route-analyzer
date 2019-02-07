package com.routeanalyzer.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Position implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal latitudeDegrees;
	private BigDecimal longitudeDegrees;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((latitudeDegrees == null) ? 0 : latitudeDegrees.hashCode());
		result = prime * result + ((longitudeDegrees == null) ? 0 : longitudeDegrees.hashCode());
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
		Position other = (Position) obj;
		if (latitudeDegrees == null) {
			if (other.latitudeDegrees != null)
				return false;
		} else if (!latitudeDegrees.equals(other.latitudeDegrees))
			return false;
		if (longitudeDegrees == null) {
			if (other.longitudeDegrees != null)
				return false;
		} else if (!longitudeDegrees.equals(other.longitudeDegrees))
			return false;
		return true;
	}

}
