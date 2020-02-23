package com.routeanalyzer.api.model.exception;

import lombok.Value;

@Value
public class FileNotFoundException extends Exception {
    private String id;
    private String type;
}
