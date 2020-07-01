package com.ee.digi_doc.web.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SignContainerRequest {

    @NotNull
    private Long signingDataId;

    @NotEmpty
    private String signatureInHex;

}
