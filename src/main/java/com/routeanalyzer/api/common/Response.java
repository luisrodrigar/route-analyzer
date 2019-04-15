package com.routeanalyzer.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {

    private Boolean error;
    private String description;
    private String errorMessage;

}
