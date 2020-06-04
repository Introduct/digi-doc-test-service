package com.ee.digi_doc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Exception.InvalidFileNameException")
public class InvalidFileNameException extends DigiDocException {

    public InvalidFileNameException(Object argument) {
        super(argument);
    }

}
