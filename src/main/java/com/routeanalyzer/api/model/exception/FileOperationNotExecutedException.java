package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class FileOperationNotExecutedException extends Exception {
    private String operationName;
    private String fileType;
}
