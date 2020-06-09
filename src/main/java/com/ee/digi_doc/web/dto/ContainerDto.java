package com.ee.digi_doc.web.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
public class ContainerDto {

    private Long id;

    private String name;

    private String url;

    private LocalDateTime createdOn;

    private String signatureInHex;

}
