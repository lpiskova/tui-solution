package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.dto.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private final HttpHeaders httpHeaders;

    public ExceptionController() {
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Value("${error.message.notFound}")
    private String notFoundMessage;
    @Value("${error.message.notAcceptable}")
    private String notAcceptableMessage;

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorMessage> handleNotFoundException() {
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.NOT_FOUND.value(), notFoundMessage);
        return new ResponseEntity<>(errorMessage, httpHeaders, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorMessage> handleMediaTypeNotAcceptableException() {
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.NOT_ACCEPTABLE.value(), notAcceptableMessage);
        return new ResponseEntity<>(errorMessage, httpHeaders, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneralException() {
        log.error("An unexpected error has occurred.");
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
