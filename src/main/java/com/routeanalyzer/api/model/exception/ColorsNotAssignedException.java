package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class ColorsNotAssignedException extends RuntimeException {
    private String activityId;
}
