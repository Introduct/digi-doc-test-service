package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.storage.local.util.HexUtils;
import com.ee.digi_doc.util.FileUtils;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.apache.commons.lang3.StringUtils;
import org.digidoc4j.Container;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ee.digi_doc.util.FileGenerator.randomFile;
import static com.ee.digi_doc.util.FileGenerator.randomTxtFile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
class SigningDataServiceTest {

    @Value("${test.file.number:10}")
    private int fileNumber;

    @Autowired
    private SigningDataService signingDataService;

    @Autowired
    private JpaSigningDataRepository jpaSigningDataRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private JpaFileRepository jpaFileRepository;

    private static StorageProperties storageProperties;

    @Autowired
    public void setStorageProperties(StorageProperties storageProperties) {
        SigningDataServiceTest.storageProperties = storageProperties;
    }

    @AfterAll
    public static void after() throws IOException {
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath());
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath());

        FileUtils.cleanUp(signingDataDirectoryPath);
        FileUtils.cleanUp(filesDirectoryPath);
    }

    @Test
    void whenCreateDataToSign_thenOk() {
        List<File> filesToSign = createFiles();
        List<Long> fileIds = filesToSign.stream().map(File::getId).collect(Collectors.toList());

        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(fileIds);
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(request);
        assertNotNull(signingData);
        assertNotNull(signingData.getId());
        assertNotNull(signingData.getContainerName());
        assertNotNull(signingData.getContainer());
        assertNotNull(signingData.getDataToSignName());
        assertNotNull(signingData.getDataToSign());
        assertNotNull(signingData.getCreatedOn());
        assertNotNull(signingData.getSignatureInHex());

        assertTrue(signingData.getContainerName().endsWith("." + Container.DocumentType.BDOC.name().toLowerCase()));
        assertTrue(signingData.getDataToSignName().endsWith(".bin"));

        assertTrue(jpaSigningDataRepository.findById(signingData.getId()).isPresent());

        assertTrue(Files.exists(getContainerPath(signingData)));
        assertTrue(Files.exists(getDataToSignPath(signingData)));

        assertTrue(jpaFileRepository.findAllById(fileIds).isEmpty());

        for (File file : filesToSign) {
            assertTrue(Files.notExists(getFilePath(file)));
        }
    }

    @Test
    void givenFileHasEmptyContentType_whenCreateDataToSign_thenOk() {
        List<File> filesToSign = new ArrayList<>();
        filesToSign.add(fileService.create(randomFile(randomAlphabetic(10), 10, null)));
        filesToSign.add(fileService.create(randomTxtFile()));

        List<Long> fileIds = filesToSign.stream().map(File::getId).collect(Collectors.toList());

        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(fileIds);
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(request);
        assertNotNull(signingData);
        assertNotNull(signingData.getId());
        assertNotNull(signingData.getContainerName());
        assertNotNull(signingData.getContainer());
        assertNotNull(signingData.getDataToSignName());
        assertNotNull(signingData.getDataToSign());
        assertNotNull(signingData.getCreatedOn());
        assertNotNull(signingData.getSignatureInHex());

        assertTrue(signingData.getContainerName().endsWith("." + Container.DocumentType.BDOC.name().toLowerCase()));
        assertTrue(signingData.getDataToSignName().endsWith(".bin"));

        assertTrue(jpaSigningDataRepository.findById(signingData.getId()).isPresent());

        assertTrue(Files.exists(getContainerPath(signingData)));
        assertTrue(Files.exists(getDataToSignPath(signingData)));

        assertTrue(jpaFileRepository.findAllById(fileIds).isEmpty());

        for (File file : filesToSign) {
            assertTrue(Files.notExists(getFilePath(file)));
        }
    }

    @Test
    void whenGetDataToSign_thenOk() {
        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(createFiles().stream().map(File::getId).collect(Collectors.toList()));
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        Long signingDataId = signingDataService.create(request).getId();

        SigningData signingData = signingDataService.getSigningData(signingDataId).orElse(null);

        assertNotNull(signingData);
        assertNotNull(signingData.getId());
        assertNotNull(signingData.getContainerName());
        assertNotNull(signingData.getContainer());
        assertNotNull(signingData.getDataToSignName());
        assertNotNull(signingData.getDataToSign());
        assertNotNull(signingData.getCreatedOn());
        assertNotNull(signingData.getSignatureInHex());

        assertTrue(signingData.getContainerName().endsWith("." + Container.DocumentType.BDOC.name().toLowerCase()));
        assertTrue(signingData.getDataToSignName().endsWith(".bin"));
    }

    @Test
    void whenDeleteDataToSign_thenOK() {
        List<Long> fileIds = createFiles().stream().map(File::getId).collect(Collectors.toList());

        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(fileIds);
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(request);

        signingDataService.delete(signingData);

        assertTrue(jpaSigningDataRepository.findById(signingData.getId()).isEmpty());

        assertTrue(Files.notExists(getContainerPath(signingData)));
        assertTrue(Files.notExists(getDataToSignPath(signingData)));
    }

    private List<File> createFiles() {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < fileNumber; i++) {
            files.add(fileService.create(randomTxtFile()));
        }
        return files;
    }

    private Path getFilePath(File file) {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath());
        String filePath = StringUtils.join(new Object[]{HexUtils.getFileHex(file), file.getName()}, "/");
        return filesDirectoryPath.resolve(filePath).normalize();
    }

    private Path getContainerPath(SigningData signingData) {
        return getPath(signingData, signingData.getContainerName());
    }

    private Path getDataToSignPath(SigningData signingData) {
        return getPath(signingData, signingData.getDataToSignName());
    }

    private Path getPath(SigningData signingData, String name) {
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath());
        String containerPath = StringUtils.join(new Object[]{HexUtils.getSigningDataHex(signingData), name}, "/");
        return signingDataDirectoryPath.resolve(containerPath).normalize();
    }

}