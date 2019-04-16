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
        if (SAXParseException.class.isInstance(originException)) {
            exception = SAXParseException.class.cast(originException);
            logMessage = "SAXParseException error: " + error.getMessage();
            description = "Problem trying to parser xml file. Check if its correct.";
            errorMessage = exception.getMessage();
            bodyBuilder = badRequest();
        } else if (JAXBException.class.isInstance(originException)) {
            exception = JAXBException.class.cast(originException);
            logMessage = "JAXBException error: " + error.getMessage();
            description = "Problem with the file format exported/uploaded.";
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(AmazonClientException.class.isInstance(originException)){
            exception = AmazonClientException.class.cast(originException);
            logMessage = "AmazonClientException error: " + error.getMessage();
            description = "Problem trying to delete/get the activity/file :: Amazon S3 Problem";
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(IOException.class.isInstance(originException)) {
            exception = IOException.class.cast(originException);
            logMessage = "IOException error: " + error.getMessage();
            description = "Problem trying to get the file :: Input/Output Problem";
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.error(logMessage);
        Response errorBody = new Response(true, description, errorMessage, toJson(exception));
        return bodyBuilder.headers(toJsonHeaders()).body(toJson(errorBody));
    };

}
