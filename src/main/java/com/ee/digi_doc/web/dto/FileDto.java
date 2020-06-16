package com.ee.digi_doc.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileDto {

    private Long id;

    private String name;

    private String url;

    private String contentType;

    private LocalDateTime uploadedOn;

}
