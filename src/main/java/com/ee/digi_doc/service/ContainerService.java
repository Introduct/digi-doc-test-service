package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.dto.ValidateContainerResultDto;
import com.ee.digi_doc.web.request.SignContainerRequest;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface ContainerService {

    Container signContainer(SignContainerRequest request);

    Optional<Container> get(Long id);

    Optional<ResponseEntity<byte[]>> getAsResponseEntry(Long id);

    Optional<ValidateContainerResultDto> validateContainer(@NotNull Long id);

    void delete(Container container);

}
