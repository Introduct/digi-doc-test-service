package com.ee.digi_doc.web.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
@ToString(exclude = "certificateInHex")
public class CreateSigningDataRequest {

    @NotEmpty
    private Long[] fileIds;

    @NotEmpty
    private String certificateInHex;

}
