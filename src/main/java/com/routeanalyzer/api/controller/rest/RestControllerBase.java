package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.controller.Response;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static com.routeanalyzer.api.common.CommonUtils.toJsonHeaders;
import static com.routeanalyzer.api.common.Constants.AMAZON_CLIENT_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.IO_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.JAXB_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.SAX_PARSE_EXCEPTION_MESSAGE;
import static java.util.Optional.of;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
public abstract class RestControllerBase {

    protected Function<RuntimeException, ResponseEntity<String>> handleControllerExceptions = error ->
            Try.of(() -> throwException(error))
                .map(success -> ok()
                        .body(JsonUtils.toJson(Response.builder().error(false).build())))
                .recover(SAXParseException.class, exception ->
                        createResponse(badRequest(), exception, SAX_PARSE_EXCEPTION_MESSAGE).orElse(null))
                .recover(JAXBException.class, exception ->
                        createResponse(status(HttpStatus.INTERNAL_SERVER_ERROR), exception, JAXB_EXCEPTION_MESSAGE)
                                .orElse(null))
                .recover(AmazonClientException.class, exception ->
                        createResponse(status(HttpStatus.INTERNAL_SERVER_ERROR), exception,
                                AMAZON_CLIENT_EXCEPTION_MESSAGE).orElse(null))
                .recover(IOException.class, exception ->
                        createResponse(status(HttpStatus.INTERNAL_SERVER_ERROR), exception,
                                IO_EXCEPTION_MESSAGE).orElse(null))
                .recover(IllegalArgumentException.class, exception ->
                        createResponse(badRequest(), exception, BAD_REQUEST_MESSAGE).orElse(null))
                .toJavaOptional()
                .orElse(null);

    private ResponseEntity<String> throwException(Throwable throwable) throws Throwable {
        throw throwable.getCause();
    }

    private Optional<ResponseEntity<String>> createResponse(ResponseEntity.BodyBuilder bodyBuilder, Exception exception,
                                                  String description) {
        log.error(exception.getMessage(), exception);
        return of(toJsonHeaders())
                .map(bodyBuilder::headers)
                .flatMap(badRequest -> of(createErrorBody(true, description, exception.getMessage(), exception))
                        .map(JsonUtils::toJson)
                        .map(badRequest::body));
    }

    private Response createErrorBody(boolean isError, String description, String errorMessage, Exception exception) {
        return Response.builder()
                .error(isError)
                .description(description)
                .errorMessage(errorMessage)
                .exception(JsonUtils.toJson(exception))
                .build();
    }

}
