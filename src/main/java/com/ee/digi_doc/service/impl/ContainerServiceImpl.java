package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.ContainerService;
import com.ee.digi_doc.service.FileSigner;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.storage.StorageContainerRepository;
import com.ee.digi_doc.web.dto.ValidateContainerResultDto;
import com.ee.digi_doc.web.request.SignContainerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Signature;
import org.digidoc4j.ValidationResult;
import org.digidoc4j.X509Cert;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.digidoc4j.X509Cert.SubjectName.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final JpaContainerRepository jpaContainerRepository;
    private final StorageContainerRepository storageContainerRepository;
    private final SigningDataService signingDataService;
    private final FileSigner fileSigner;

    @Override
    @Transactional
    public Container signContainer(SignContainerRequest request) {
        log.info("Sign container by data to sign id: {}", request.getSigningDataId());
        SigningData signingData = signingDataService.getSigningData(request.getSigningDataId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getSigningDataId()));

        log.debug("Data to sigh: {}", signingData);

        FileSigner.Container signedContainer = fileSigner
                .signContainer(signingData.getGeneratedSigningData(), request.getSignatureInHex());

        Container container = new Container();
        container.setBdDocContainer(signedContainer.getBdDocContainer());
        container.setContentType(signedContainer.getContentType());
        log.debug("Container has been signed, container: {}", container);

        container = jpaContainerRepository.saveAndFlush(container);
        log.debug("Container has been saved in database");

        storageContainerRepository.storeContainer(container);
        log.debug("Container has been stored to local storage");

        signingDataService.delete(signingData);

        return container;
    }

    @Override
    public Optional<Container> get(Long id) {
        log.info("Get container by id: {}", id);
        return jpaContainerRepository.findById(id)
                .map(container -> {
                    log.debug("Container has been found in database");
                    container.setBdDocContainer(storageContainerRepository.getContainer(container));
                    log.debug("DBDoc container has been found in local storage");
                    return container;
                });
    }

    @Override
    public Optional<ResponseEntity<byte[]>> getAsResponseEntry(Long id) {
        return get(id)
                .map(container -> {
                    try {
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(container.getContentType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + container.getName())
                                .body(container.getContent());
                    } catch (IOException e) {
                        log.error("Error obtained during bdoc container read", e);
                        throw new FileNotReadException(container.getId());
                    }
                });
    }

    @Override
    public Optional<ValidateContainerResultDto> validateContainer(@NotNull Long id) {
        return get(id)
                .map(Container::getBdDocContainer)
                .map(org.digidoc4j.Container::getSignatures)
                .map(Collection::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next)
                .map(this::createValidateResult);
    }

    @Override
    @Transactional
    public void delete(Container container) {
        log.info("Delete container: {}", container);

        jpaContainerRepository.delete(container);
        log.debug("Container has been removed from database");

        storageContainerRepository.deleteContainer(container);
        log.debug("Container has been removed from local storage");
    }

    private ValidateContainerResultDto createValidateResult(Signature signature) {
        LocalDateTime signedOn = signature.getClaimedSigningTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        ValidationResult validationResult = signature.validateSignature();
        boolean valid = validationResult.isValid();
        X509Cert signingCertificate = signature.getSigningCertificate();
        String signerIdCode = signingCertificate.getSubjectName(SERIALNUMBER);
        String signerFirstName = signingCertificate.getSubjectName(GIVENNAME);
        String signerLastName = signingCertificate.getSubjectName(SURNAME);
        String signerCountryCode = signingCertificate.getSubjectName(C);
        return new ValidateContainerResultDto(valid, signerIdCode, signerFirstName, signerLastName,
                signerCountryCode, signedOn);
    }
}
