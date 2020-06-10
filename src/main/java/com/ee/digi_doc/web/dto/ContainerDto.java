package com.ee.digi_doc.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContainerDto {

    private Long id;

    private String name;

    private LocalDateTime signedOn;

    private String url;

}
