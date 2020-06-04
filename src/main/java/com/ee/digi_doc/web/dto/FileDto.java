package com.ee.digi_doc.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDto {

    private Long id;

    private String fileName;

    private String contentType;

    private LocalDateTime uploadedOn;

    private String url;

}
