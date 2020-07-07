package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.ValidateContainerResultDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.ee.digi_doc.web.request.SignContainerRequest;
import org.digidoc4j.DigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ee.digi_doc.storage.local.LocalStorageContainerRepository.getUniqueContainerName;
import static com.ee.digi_doc.storage.local.LocalStorageSigningDataRepository.getUniqueContainerName;
import static com.ee.digi_doc.storage.local.LocalStorageSigningDataRepository.getUniqueDataToSignName;
import static com.ee.digi_doc.util.FileGenerator.randomFile;
import static com.ee.digi_doc.util.FileGenerator.randomTxtFile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ContainerServiceTest {

    @Value("${test.file.number:10}")
    private int fileNumber;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private SigningDataService signingDataService;

    @Autowired
    private FileService fileService;

    @Autowired
    private JpaContainerRepository jpaContainerRepository;

    @Autowired
    private StorageProperties storageProperties;

    @Test
    void whenSighContainer_thenOk() {
        Path containerDirectoryPath = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath()).toAbsolutePath().normalize();

        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds());
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container container = containerService.signContainer(signContainerRequest);

        assertNotNull(container);
        assertNotNull(container.getId());
        assertNotNull(container.getName());
        assertNotNull(container.getContentType());
        assertNotNull(container.getSignedOn());

        assertEquals(signingData.getContainerName(), container.getName());
        assertEquals("application/vnd.etsi.asic-e+zip", container.getContentType());

        assertTrue(jpaContainerRepository.findById(container.getId()).isPresent());
        assertTrue(Files.exists(containerDirectoryPath.resolve(getUniqueContainerName(container))));

        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(getUniqueContainerName(signingData))));
        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(getUniqueDataToSignName(signingData))));
    }

    @Test
    void givenFileHasEmptyContentType_whenSighContainer_thenOk() {
        Path containerDirectoryPath = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath()).toAbsolutePath().normalize();

        List<File> filesToSign = new ArrayList<>();
        filesToSign.add(fileService.create(randomFile(randomAlphabetic(10), 10, null)));
        filesToSign.add(fileService.create(randomTxtFile()));

        List<Long> fileIds = filesToSign.stream().map(File::getId).collect(Collectors.toList());

        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(fileIds);
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container container = containerService.signContainer(signContainerRequest);

        assertNotNull(container);
        assertNotNull(container.getId());
        assertNotNull(container.getName());
        assertNotNull(container.getContentType());
        assertNotNull(container.getSignedOn());

        assertEquals(signingData.getContainerName(), container.getName());
        assertEquals("application/vnd.etsi.asic-e+zip", container.getContentType());

        assertTrue(jpaContainerRepository.findById(container.getId()).isPresent());
        assertTrue(Files.exists(containerDirectoryPath.resolve(getUniqueContainerName(container))));

        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(getUniqueContainerName(signingData))));
        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(getUniqueDataToSignName(signingData))));
    }

    @Test
    void whenGetSignedContainer_thenOk() {
        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds());
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container signedContainer = containerService.signContainer(signContainerRequest);

        Long containerId = signedContainer.getId();

        Container container = containerService.get(containerId).orElse(null);

        assertNotNull(container);
        assertNotNull(container.getId());
        assertNotNull(container.getName());
        assertNotNull(container.getContentType());
        assertNotNull(container.getSignedOn());

        assertEquals(signingData.getContainerName(), container.getName());
        assertEquals("application/vnd.etsi.asic-e+zip", container.getContentType());
    }

    @Test
    void whenValidContainer_thenOk() {
        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds());
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container signedContainer = containerService.signContainer(signContainerRequest);

        Long containerId = signedContainer.getId();

        ValidateContainerResultDto result = containerService.validateContainer(containerId).orElse(null);

        assertNotNull(result);
        assertNotNull(result.getSignerIdCode());
        assertNotNull(result.getSignerFirstName());
        assertNotNull(result.getSignerLastName());
        assertNotNull(result.getSignerCountryCode());
        assertNotNull(result.getSignedOn());

        assertTrue(result.isValid());
    }

    @Test
    void whenDeleteContainer_thenOk() {
        Path containerDirectoryPath = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();

        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds());
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container container = containerService.signContainer(signContainerRequest);

        assertTrue(jpaContainerRepository.findById(container.getId()).isPresent());
        assertTrue(Files.exists(containerDirectoryPath.resolve(getUniqueContainerName(container))));

        containerService.delete(container);

        assertTrue(jpaContainerRepository.findById(container.getId()).isEmpty());
        assertTrue(Files.notExists(containerDirectoryPath.resolve(getUniqueContainerName(container))));
    }

    private List<Long> createFileIds() {
        List<Long> files = new ArrayList<>();
        for (int i = 0; i < fileNumber; i++) {
            files.add(fileService.create(FileGenerator.randomTxtFile()).getId());
        }
        return files;
    }

}