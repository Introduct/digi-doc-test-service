package com.ee.digi_doc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequiredArgsConstructor
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Exception.FileNotFoundException")
public class FileNotFoundException extends RuntimeException{

    @Getter
    private final String fileName;

}
