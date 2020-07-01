package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.mapper.ContainerMapper;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.service.ContainerService;
import com.ee.digi_doc.web.dto.ContainerDto;
import com.ee.digi_doc.web.dto.ValidateContainerResultDto;
import com.ee.digi_doc.web.request.SignContainerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/containers")
public class ContainerRestController {

    private final ContainerService containerService;
    private final ContainerMapper containerMapper;

    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ContainerDto signContainer(@Valid @RequestBody SignContainerRequest request) {
        Container container = containerService.signContainer(request);
        return containerMapper.toDto(container);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getContainer(@PathVariable Long id) {
        Container container = containerService.get(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(container.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + container.getName())
                .body(container.getContent());
    }

    @GetMapping(value = "/{id}/validate", produces = APPLICATION_JSON_VALUE)
    public ValidateContainerResultDto validate(@PathVariable Long id) {
        return containerService.validateContainer(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

}
