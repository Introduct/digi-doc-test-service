package com.ee.digi_doc.common;

import com.ee.digi_doc.exception.DigiDocException;
import com.ee.digi_doc.exception.dto.DigiDocApiError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(DigiDocException.class)
    public ResponseEntity<DigiDocApiError> handleAuthenticationException(Exception ex, WebRequest request) {
        log.error("Handled exception for request: {}", request, ex);
        Class<? extends Exception> exceptionClass = ex.getClass();
        ResponseStatus annotation = exceptionClass.getAnnotation(ResponseStatus.class);
        HttpStatus code = annotation.code();
        String reason = annotation.reason();
        String message = getMessage(annotation.reason(), request.getLocale(), ((DigiDocException) ex).getArgument());
        log.debug("Response error code: {}, reason: {}, message: {}", code, reason, message);
        return ResponseEntity.status(code).body(new DigiDocApiError(message));
    }


    private String getMessage(String messageCode, Locale locale, Object... parameters) {
        return messageSource.getMessage(messageCode, parameters, messageCode, locale);
    }

}
