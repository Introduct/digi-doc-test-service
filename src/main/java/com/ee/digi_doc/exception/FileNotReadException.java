package com.ee.digi_doc.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequiredArgsConstructor
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Exception.FileNotReadException")
public class FileNotReadException extends RuntimeException {
}
