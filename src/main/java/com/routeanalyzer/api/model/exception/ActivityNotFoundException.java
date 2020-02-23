package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class ActivityNotFoundException extends Exception {
    private String activityId;
}
