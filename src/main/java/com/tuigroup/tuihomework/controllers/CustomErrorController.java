package com.tuigroup.tuihomework.controllers;

import com.tuigroup.tuihomework.view.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @Value("${error.message.notFound}")
    private String notFoundMessage;
    @Value("${error.message.notAcceptable}")
    private String notAcceptableMessage;

    @Operation(hidden = true)
    @RequestMapping(value = "/error", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleError(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<ErrorMessage> internalServerResponseEntity =
                new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
                        httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);

        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).map(status -> {
                    int statusCode = Integer.parseInt(status.toString());
                    if (statusCode == HttpStatus.NOT_FOUND.value()) {
                        ErrorMessage errorMessage = new ErrorMessage(statusCode, notFoundMessage);
                        return new ResponseEntity<>(errorMessage, httpHeaders, HttpStatus.NOT_FOUND);
                    } else if (statusCode == HttpStatus.NOT_ACCEPTABLE.value()) {
                        ErrorMessage errorMessage = new ErrorMessage(statusCode, notAcceptableMessage);
                        return new ResponseEntity<>(errorMessage, httpHeaders, HttpStatus.NOT_ACCEPTABLE);
                    }
                    return internalServerResponseEntity;
                }
        ).orElse(internalServerResponseEntity);
    }

}