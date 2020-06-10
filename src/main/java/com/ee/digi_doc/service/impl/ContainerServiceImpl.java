package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.ContainerService;
import com.ee.digi_doc.service.FileSigner;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.storage.LocalStorageContainerRepository;
import com.ee.digi_doc.web.request.SignContainerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final JpaContainerRepository jpaContainerRepository;
    private final LocalStorageContainerRepository localStorageContainerRepository;
    private final SigningDataService signingDataService;
    private final FileSigner fileSigner;

    @Override
    @Transactional
    public Container signContainer(Long signingDataId, SignContainerRequest request) {
        log.info("Sign container by data to sign id: {}", signingDataId);

        SigningData signingData = signingDataService.getSigningData(signingDataId)
                .orElseThrow(() -> new ResourceNotFoundException(signingDataId));
        log.debug("Data to sigh: {}", signingData);

        Container container = fileSigner.signContainer(signingData, request.getSignatureInHex());
        log.debug("Container has been signed, container: {}", container);

        container = jpaContainerRepository.saveAndFlush(container);
        log.debug("Container has been saved in database");

        localStorageContainerRepository.storeContainer(container);
        log.debug("Container has been stored to local storage");

        signingDataService.delete(signingData.getId());

        return container;
    }

    @Override
    public Optional<Container> get(Long id) {
        log.info("Get container by id: {}", id);
        return jpaContainerRepository.findById(id)
                .map(container -> {
                    log.debug("Container has been found in database");
                    container.setBdDocContainer(localStorageContainerRepository.getContainer(container.getName()));
                    log.debug("DBDoc container has been found in local storage");
                    return container;
                });
    }
}
