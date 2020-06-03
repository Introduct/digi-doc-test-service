package com.ee.digi_doc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequiredArgsConstructor
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Exception.InvalidFileNameException")
public class InvalidFileNameException extends RuntimeException {

    @Getter
    private final String fileName;

}
