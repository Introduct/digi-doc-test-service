package com.ee.digi_doc.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDto {

    private Long id;

    private String name;

    private String url;

    private String contentType;

    private LocalDateTime uploadedOn;

}
