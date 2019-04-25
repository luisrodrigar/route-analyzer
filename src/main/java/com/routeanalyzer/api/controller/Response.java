package com.routeanalyzer.api.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Response {

    private Boolean error;
    private String description;
    private String errorMessage;
    private String exception;

}
