package com.ee.digi_doc.web.request;

import com.ee.digi_doc.web.validator.ValidFileIds;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CreateSigningDataRequest {

    @NotEmpty
    @ValidFileIds
    private List<Long> fileIds;

    @NotEmpty
    private String certificateInHex;

}
