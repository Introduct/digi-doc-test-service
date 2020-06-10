package com.ee.digi_doc.web.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SignContainerRequest {

    @NotEmpty
    private String signatureInHex;

}
