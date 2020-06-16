package com.ee.digi_doc.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ContainerDto {

    private Long id;

    private String name;

    private LocalDateTime signedOn;

    private String url;

}
