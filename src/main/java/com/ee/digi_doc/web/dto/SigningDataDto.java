package com.ee.digi_doc.web.dto;

import lombok.Data;

@Data
public class SigningDataDto {

    private Long id;

    private String containerName;

    private String dataToSignName;

    private String signatureInHex;

}
