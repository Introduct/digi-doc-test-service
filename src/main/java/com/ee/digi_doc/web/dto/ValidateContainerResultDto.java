package com.ee.digi_doc.web.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ValidateContainerResultDto {

    private final boolean valid;
    private final String signerIdCode;
    private final String signerFirstName;
    private final String signerLastName;
    private final String signerCountryCode;
    private final LocalDateTime signedOn;

}
