package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class ActivityOperationNotExecutedException extends Exception {
    private String activityId;
    private String operationName;
}
