package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.mapper.SigningDataMapper;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/signing-data")
public class SigningDataRestController {

    private final SigningDataService signingDataService;
    private final SigningDataMapper signingDataMapper;

    @PostMapping
    public SigningDataDto createSigningData(@Valid @RequestBody CreateSigningDataRequest request) {
        SigningData signingData = signingDataService.create(request);
        return signingDataMapper.toDto(signingData);
    }

    @GetMapping("/{id}")
    public SigningDataDto getSigningData(@PathVariable Long id) {
        return signingDataService.getSigningData(id)
                .map(signingDataMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

}
