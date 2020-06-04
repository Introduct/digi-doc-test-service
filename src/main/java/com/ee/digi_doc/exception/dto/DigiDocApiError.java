package com.ee.digi_doc.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DigiDocApiError {

    @Getter
    private final String message;

}
