package com.ee.digi_doc.web.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
public class CreateSigningDataRequest {

    @NotEmpty
    private Long[] fileIds;

    @NotEmpty
    private String certificateInHex;

}
