package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class ActivityNotFoundException extends RuntimeException {
    private String activityId;
}
