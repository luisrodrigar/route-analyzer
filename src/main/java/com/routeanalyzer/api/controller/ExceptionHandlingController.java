package com.routeanalyzer.api.controller;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.model.exception.ActivityColorsNotAssignedException;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityOperationNoExecutedException;
import com.routeanalyzer.api.model.exception.FileNotFoundException;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.xml.sax.SAXParseException;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import static com.routeanalyzer.api.common.Constants.ACTIVITY_NOT_FOUND;
import static com.routeanalyzer.api.common.Constants.AMAZON_CLIENT_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.COLORS_ASSIGNED_EXCEPTION;
import static com.routeanalyzer.api.common.Constants.FILE_NOT_FOUND;
import static com.routeanalyzer.api.common.Constants.IO_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.JAXB_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.OPERATION_NOT_EXECUTED;
import static com.routeanalyzer.api.common.Constants.SAX_PARSE_EXCEPTION_MESSAGE;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingController {

    @ResponseBody
    @ExceptionHandler(SAXParseException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleSAXParseException(final Exception exception) {
        log.warn("SAXParse error happened: ", exception);
        return createErrorBody(true, SAX_PARSE_EXCEPTION_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(JAXBException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleJAXBException(final Exception exception) {
        log.warn("JAXB error happened: ", exception);
        return createErrorBody(true, JAXB_EXCEPTION_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(AmazonClientException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleAmazonClientException(final Exception exception) {
        log.warn("Amazon Client error happened: ", exception);
        return createErrorBody(true, AMAZON_CLIENT_EXCEPTION_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(IOException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleIOException(final Exception exception) {
        log.warn("Input/output error happened: ", exception);
        return createErrorBody(true, IO_EXCEPTION_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    Response handleIllegalArgumentException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, BAD_REQUEST_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(ActivityNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    Response handleActivityNotFoundException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, ACTIVITY_NOT_FOUND, exception);
    }

    @ResponseBody
    @ExceptionHandler(ActivityColorsNotAssignedException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleColorsNotAssignedException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, COLORS_ASSIGNED_EXCEPTION, exception);
    }

    @ResponseBody
    @ExceptionHandler(ActivityOperationNoExecutedException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleOperationNotExecutedException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, OPERATION_NOT_EXECUTED, exception);
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    Response handleConstraintViolationException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, BAD_REQUEST_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    Response handleErrorTypeParamsException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, BAD_REQUEST_MESSAGE, exception);
    }

    @ResponseBody
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    Response handleFileNotFoundException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, FILE_NOT_FOUND, exception);
    }

    @ResponseBody
    @ExceptionHandler(FileOperationNotExecutedException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    Response handleFileOperationNotExecutedException(final Exception exception) {
        log.warn("Params error happened: ", exception);
        return createErrorBody(true, OPERATION_NOT_EXECUTED, exception);
    }

    private Response createErrorBody(final boolean isError, final String description, final Exception exception) {
        return Response.builder()
                .error(isError)
                .description(description)
                .errorMessage(exception.getMessage())
                .exception(JsonUtils.toJson(exception)
                        .getOrNull())
                .build();
    }

}
