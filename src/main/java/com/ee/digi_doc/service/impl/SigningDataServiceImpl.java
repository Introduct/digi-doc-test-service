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
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SigningDataServiceImpl implements SigningDataService {

    private final JpaSigningDataRepository jpaSigningDataRepository;
    private final LocalStorageSigningDataRepository localStorageSigningDataRepository;
    private final FileService fileService;
    private final FileSigner fileSigner;

    @Override
    public SigningData create(CreateSigningDataRequest request) {
        List<File> filesToSign = Stream.of(request.getFileIds())
                .map(fileService::get)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        SigningData signingData = fileSigner.generateDataToSign(filesToSign, request.getCertificateInHex());

        signingData = jpaSigningDataRepository.saveAndFlush(signingData);
        localStorageSigningDataRepository.storeSigningData(signingData);

        filesToSign.stream().map(File::getId).forEach(fileService::delete);

        return signingData;
    }

    @Override
    public Optional<SigningData> getSigningData(@NotNull Long id) {
        return jpaSigningDataRepository.findById(id)
                .map(signingData -> {
                    signingData.setContainer(localStorageSigningDataRepository.getContainer(signingData.getContainerName()));
                    signingData.setDataToSign(localStorageSigningDataRepository.getDataToSign(signingData.getDataToSignName()));
                    return signingData;
                });
    }

}
