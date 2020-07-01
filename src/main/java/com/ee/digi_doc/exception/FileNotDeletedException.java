package com.ee.digi_doc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Exception.FileNotDeletedException")
public class FileNotDeletedException extends DigiDocException {

    public FileNotDeletedException(Object argument) {
        super(argument);
    }

}
