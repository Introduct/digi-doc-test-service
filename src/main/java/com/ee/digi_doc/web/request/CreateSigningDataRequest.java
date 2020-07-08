package com.ee.digi_doc.web.request;

import com.ee.digi_doc.web.validator.ValidExistingFileIds;
import com.ee.digi_doc.web.validator.ValidFileIdsCount;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CreateSigningDataRequest {

    @NotEmpty
    @ValidExistingFileIds
    @ValidFileIdsCount
    private List<Long> fileIds;

    @NotEmpty
    private String certificateInHex;

}
