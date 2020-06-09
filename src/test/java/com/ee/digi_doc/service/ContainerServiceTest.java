package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.ContainerRepository;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.request.CreateContainerRequest;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
class ContainerServiceTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    private ContainerService containerService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private JpaFileRepository jpaFileRepository;

    @Autowired
    private StorageProperties storageProperties;

    @Test
    void whenGenerateDataToSign_thenOk() {
        Path fileDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();
        Path containerDirectoryPath = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();

        List<File> files = createFiles();

        CreateContainerRequest request = createRequest(files, TestSigningData.getRSASigningCertificateInHex());

        Container container = containerService.create(request);

        assertNotNull(container);
        assertNotNull(container.getId());
        assertNotNull(container.getName());
        assertNotNull(container.getContent());
        assertNotNull(container.getCreatedOn());
        assertNotNull(container.getSignatureInHex());

        assertEquals(now().format(DATE_TIME_FORMATTER), container.getCreatedOn().format(DATE_TIME_FORMATTER));

        assertTrue(containerRepository.findById(container.getId()).isPresent());
        assertTrue(Files.exists(containerDirectoryPath.resolve(container.getName())));

        assertTrue(jpaFileRepository.findAllById(Stream.of(request.getFileIds()).collect(Collectors.toList())).isEmpty());
        for (File file : files) {
            assertTrue(Files.notExists(fileDirectoryPath.resolve(file.getName())));
        }
    }


    private List<File> createFiles() {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            files.add(fileService.create(FileGenerator.randomMultipartJpeg()));
        }
        return files;
    }

    private CreateContainerRequest createRequest(Collection<File> files, String certificateInHex) {
        CreateContainerRequest request = new CreateContainerRequest();

        request.setFileIds(files.stream().map(File::getId).collect(Collectors.toList()).toArray(Long[]::new));
        request.setCertificateInHex(certificateInHex);

        return request;
    }

}