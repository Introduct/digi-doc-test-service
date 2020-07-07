package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.mapper.SigningDataMapper;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.ee.digi_doc.web.validator.CreateSigningDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/signing-data")
public class SigningDataRestController {

    private final CreateSigningDataValidator signingDataValidator;
    private final SigningDataService signingDataService;
    private final SigningDataMapper signingDataMapper;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(signingDataValidator);
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public SigningDataDto createSigningData(@Valid @RequestBody CreateSigningDataRequest request) {
        SigningData signingData = signingDataService.create(request);
        return signingDataMapper.toDto(signingData);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public SigningDataDto getSigningData(@PathVariable Long id) {
        return signingDataService.getSigningData(id)
                .map(signingDataMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

}
