package com.routeanalyzer.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal latitudeDegrees;
	private BigDecimal longitudeDegrees;
	
}
