package com.routeanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class Position implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal latitudeDegrees, longitudeDegrees;
	
}
