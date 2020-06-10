package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.FileSigner;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.storage.LocalStorageSigningDataRepository;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SigningDataServiceImpl implements SigningDataService {

    private final JpaSigningDataRepository jpaSigningDataRepository;
    private final LocalStorageSigningDataRepository localStorageSigningDataRepository;
    private final FileService fileService;
    private final FileSigner fileSigner;

    @Override
    @Transactional
    public SigningData create(CreateSigningDataRequest request) {
        log.info("Create data to sign for files: {}", Arrays.toString(request.getFileIds()));

        List<File> filesToSign = Stream.of(request.getFileIds())
                .map(fileService::get)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        log.debug("Files to be signed: {}", filesToSign);

        SigningData signingData = fileSigner.generateDataToSign(filesToSign, request.getCertificateInHex());
        signingData = jpaSigningDataRepository.saveAndFlush(signingData);
        log.debug("Data to sign: {}", signingData);

        localStorageSigningDataRepository.storeSigningData(signingData);
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

                    signingData.setContainer(localStorageSigningDataRepository.getContainer(signingData.getContainerName()));
                    log.debug("DBDoc container has been found in local storage");

                    signingData.setDataToSign(localStorageSigningDataRepository.getDataToSign(signingData.getDataToSignName()));
                    log.debug("Data to sign has been found in local storage");

                    return signingData;
                });
    }

    @Override
    public void delete(@NotNull Long id) {
        log.info("Delete data to sign by id: {}", id);
        jpaSigningDataRepository.findById(id)
                .ifPresent(signingData -> {
                    log.debug("Data to sign has been found in database");
                    jpaSigningDataRepository.delete(signingData);
                    log.debug("Data to sign has been removed from database");
                    localStorageSigningDataRepository.deleteContainer(signingData.getContainerName());
                    log.debug("Container has been removed from local storage");
                    localStorageSigningDataRepository.deleteDataToSigh(signingData.getDataToSignName());
                    log.debug("Data to sigh has been removed from local storage");
                });
    }

}
