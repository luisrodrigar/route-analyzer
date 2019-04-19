package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.api.common.Response;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.function.Function;

import static com.routeanalyzer.api.common.CommonUtils.toJsonHeaders;
import static com.routeanalyzer.api.common.Constants.AMAZON_CLIENT_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.IO_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.JAXB_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.SAX_PARSE_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.JsonUtils.toJson;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;


public abstract class RestControllerBase {

    protected Logger log;

    protected RestControllerBase(Logger log) {
        this.log = log;
    }

    protected Function<RuntimeException, ResponseEntity<String>> handleControllerExceptions = error -> {
        String logMessage = null;
        String description = null;
        String errorMessage = null;
        Exception exception = null;
        Throwable originException = error.getCause();
        ResponseEntity.BodyBuilder bodyBuilder = null;
        if (originException instanceof SAXParseException) {
            exception = (SAXParseException) originException;
            logMessage = "SAXParseException error: " + error.getMessage();
            description = SAX_PARSE_EXCEPTION_MESSAGE;
            errorMessage = exception.getMessage();
            bodyBuilder = badRequest();
        } else if (originException instanceof JAXBException) {
            exception = (JAXBException) originException;
            logMessage = "JAXBException error: " + error.getMessage();
            description = JAXB_EXCEPTION_MESSAGE;
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(originException instanceof AmazonClientException){
            exception = (AmazonClientException) originException;
            logMessage = "AmazonClientException error: " + error.getMessage();
            description = AMAZON_CLIENT_EXCEPTION_MESSAGE;
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(originException instanceof IOException) {
            exception = (IOException) originException;
            logMessage = "IOException error: " + error.getMessage();
            description = IO_EXCEPTION_MESSAGE;
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(originException instanceof IllegalArgumentException) {
            exception = (IllegalArgumentException) originException;
            logMessage = "Illegal Argument Exception error: " + error.getMessage();
            description = BAD_REQUEST_MESSAGE;
            errorMessage = exception.getMessage();
            bodyBuilder = badRequest();
        }
        log.error(logMessage);
        Response errorBody = new Response(true, description, errorMessage, toJson(exception));
        return bodyBuilder.headers(toJsonHeaders()).body(toJson(errorBody));
    };

}
