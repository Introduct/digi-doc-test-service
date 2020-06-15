package com.ee.digi_doc.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigningDataDto {

    private Long id;

    private String containerName;

    private String dataToSignName;

    private String signatureInHex;

}
