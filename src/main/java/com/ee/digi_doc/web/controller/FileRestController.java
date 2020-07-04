package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.mapper.FileMapper;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.web.dto.FileDto;
import com.ee.digi_doc.web.request.UploadFileRequest;
import com.ee.digi_doc.web.validator.FileUploadValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/files")
public class FileRestController {

    private final FileUploadValidator fileUploadValidator;
    private final FileService fileService;
    private final FileMapper fileMapper;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(fileUploadValidator);
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public FileDto create(@Valid UploadFileRequest request) {
        File file = fileService.create(request.getFile());
        return fileMapper.toDto(file);
    }


    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable Long id) {
        File file = fileService.get(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .body(file.getContent());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fileService.delete(id);
    }

}
