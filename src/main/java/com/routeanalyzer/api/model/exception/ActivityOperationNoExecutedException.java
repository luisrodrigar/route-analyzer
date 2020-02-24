package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class ActivityOperationNoExecutedException extends Exception {
    private String activityId;
    private String operationName;
}
