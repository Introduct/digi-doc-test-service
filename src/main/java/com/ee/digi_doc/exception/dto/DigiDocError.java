package com.ee.digi_doc.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DigiDocError {

    @Getter
    private final String errorMessage;

}
