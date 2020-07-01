package com.ee.digi_doc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Exception.ResourceNotFoundException")
public class ResourceNotFoundException extends DigiDocException {

    public ResourceNotFoundException(Object argument) {
        super(argument);
    }

}
