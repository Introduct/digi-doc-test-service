package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ContainerServiceTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

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
        createDataToSignRequest.setFileIds(createFileIds().toArray(Long[]::new));
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
        assertEquals(now().format(DATE_TIME_FORMATTER), container.getSignedOn().format(DATE_TIME_FORMATTER));

        assertTrue(jpaContainerRepository.findById(container.getId()).isPresent());
        assertTrue(Files.exists(containerDirectoryPath.resolve(container.getName())));

        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(signingData.getContainerName())));
        assertTrue(Files.notExists(signingDataDirectoryPath.resolve(signingData.getDataToSignName())));
    }

    @Test
    void whenGetSignedContainer_thenOk() {
        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds().toArray(Long[]::new));
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Long containerId = containerService.signContainer(signContainerRequest).getId();

        Container container = containerService.get(containerId).orElse(null);

        assertNotNull(container);
        assertNotNull(container.getId());
        assertNotNull(container.getName());
        assertNotNull(container.getContentType());
        assertNotNull(container.getSignedOn());

        assertEquals(signingData.getContainerName(), container.getName());
        assertEquals("application/vnd.etsi.asic-e+zip", container.getContentType());
        assertEquals(now().format(DATE_TIME_FORMATTER), container.getSignedOn().format(DATE_TIME_FORMATTER));
    }

    @Test
    void whenValidContainer_thenOk() {
        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(createFileIds().toArray(Long[]::new));
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Long containerId = containerService.signContainer(signContainerRequest).getId();

        ValidateContainerResultDto result = containerService.validateContainer(containerId).orElse(null);

        assertNotNull(result);
        assertNotNull(result.getSignerIdCode());
        assertNotNull(result.getSignerFirstName());
        assertNotNull(result.getSignerLastName());
        assertNotNull(result.getSignerCountryCode());
        assertNotNull(result.getSignedOn());

        assertTrue(result.isValid());
    }

    private List<Long> createFileIds() {
        List<Long> files = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            files.add(fileService.create(FileGenerator.randomMultipartJpeg()).getId());
        }
        return files;
    }


}