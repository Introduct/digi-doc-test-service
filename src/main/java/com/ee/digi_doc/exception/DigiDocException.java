package com.ee.digi_doc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public abstract class DigiDocException extends RuntimeException {

    @Getter
    private final Object argument;

}
