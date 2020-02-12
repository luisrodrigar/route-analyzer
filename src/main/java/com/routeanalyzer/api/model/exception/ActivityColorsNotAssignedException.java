package com.routeanalyzer.api.model.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class ActivityColorsNotAssignedException extends RuntimeException {
    private String activityId;
}
