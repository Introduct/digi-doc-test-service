package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.FileSigner;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.storage.StorageSigningDataRepository;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SigningDataServiceImpl implements SigningDataService {

    private final JpaSigningDataRepository jpaSigningDataRepository;
    private final StorageSigningDataRepository storageSigningDataRepository;
    private final FileService fileService;
    private final FileSigner fileSigner;

    @Override
    @Transactional
    public SigningData create(CreateSigningDataRequest request) {
        log.info("Create data to sign for files: {}", request.getFileIds());

        List<File> filesToSign = request.getFileIds().stream()
                .map(fileService::get)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        log.debug("Files to be signed: {}", filesToSign);

        FileSigner.SigningData generatedDataToSign = fileSigner
                .generateDataToSign(filesToSign, request.getCertificateInHex());

        SigningData signingData = new SigningData();
        signingData.setGeneratedSigningData(generatedDataToSign);

        signingData = jpaSigningDataRepository.saveAndFlush(signingData);
        log.debug("Data to sign: {}", signingData);

        storageSigningDataRepository.storeSigningData(signingData);
        log.debug("Data to sigh has been stored to local storage");

        filesToSign.stream().map(File::getId).forEach(fileService::delete);
        log.debug("Files have been removed");

        return signingData;
    }

    @Override
    public Optional<SigningData> getSigningData(@NotNull Long id) {
        log.info("Get data to sigh by id: {}", id);
        return jpaSigningDataRepository.findById(id)
                .map(signingData -> {
                    log.debug("Data to sign has been found in database");

                    Container container = storageSigningDataRepository.getContainer(signingData);
                    log.debug("DBDoc container has been found in local storage");

                    DataToSign dataToSign = storageSigningDataRepository.getDataToSign(signingData);
                    log.debug("Data to sign has been found in local storage");

                    signingData.setGeneratedSigningData(new FileSigner.SigningData(container, dataToSign));
                    
                    return signingData;
                });
    }

    @Override
    public void delete(SigningData signingData) {
        log.info("Delete data to sign: {}", signingData);

        jpaSigningDataRepository.delete(signingData);
        log.debug("Data to sign has been removed from database");

        storageSigningDataRepository.deleteContainer(signingData);
        log.debug("Container has been removed from local storage");

        storageSigningDataRepository.deleteDataToSigh(signingData);
        log.debug("Data to sigh has been removed from local storage");
    }
}
