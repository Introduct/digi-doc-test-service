package com.ee.digi_doc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Exception.FileNotFoundException")
public class FileNotFoundException extends DigiDocException {

    public FileNotFoundException(Object argument) {
        super(argument);
    }

}
