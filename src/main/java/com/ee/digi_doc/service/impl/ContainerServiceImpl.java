package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.dao.ContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.ContainerService;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.FileSigner;
import com.ee.digi_doc.web.request.CreateContainerRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.digidoc4j.utils.Helper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ContainerServiceImpl implements ContainerService {

    private static final String CONTAINER_FILE_EXTENSION = "asice";

    private final ContainerRepository containerRepository;
    private final FileService fileService;
    private final FileSigner fileSigner;
    private final Path containerStorageLocation;

    public ContainerServiceImpl(ContainerRepository containerRepository, FileService fileService,
                                FileSigner fileSigner, StorageProperties storageProperties) {
        this.containerRepository = containerRepository;
        this.fileService = fileService;
        this.fileSigner = fileSigner;
        this.containerStorageLocation = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        if (Files.notExists(containerStorageLocation)) {
            log.info("Directory {} does not exist", containerStorageLocation.toAbsolutePath());
            try {
                log.debug("Going to create directory {}", containerStorageLocation.toAbsolutePath());
                Files.createDirectories(containerStorageLocation);
                log.info("Directory {} has been created", containerStorageLocation.toAbsolutePath());
            } catch (IOException e) {
                log.error("Exception obtained during directory creation", e);
                throw new RuntimeException("Could not create directory for containers", e);
            }
        }
    }

    @Override
    @Transactional
    public Container create(CreateContainerRequest request) {
        log.info("Create container: {}", request);

        List<File> files = Stream.of(request.getFileIds()).map(fileService::get).collect(Collectors.toList());
        log.debug("Files to be signed: {}", files);

        FileSigner.SigningData signingData = fileSigner.generateDataToSign(files, request.getCertificateInHex());

        Container container = containerRepository.saveAndFlush(createContainer(signingData, generateContainerName()));
        log.debug("Container has been successfully created, container: {}", container);

        files.stream().map(File::getId).forEach(fileService::delete);

        try {
            Files.write(containerStorageLocation.resolve(container.getName()), container.getContent());
            log.debug("Container has been successfully written");

        } catch (IOException e) {
            log.error("Exception obtained during container create", e);
            throw new FileNotWrittenException(container.getName());
        }

        return container;
    }

    @Override
    public Container get(@NotNull Long id) {
        log.info("Get container by id: {}", id);

        Container container = containerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        log.debug("Container has been found in database, container: {}", container);

        Path containerPath = containerStorageLocation.resolve(container.getName()).normalize();
        log.debug("Container path: {}", containerPath.toAbsolutePath());

        if (Files.notExists(containerPath)) {
            log.debug("Container does not exist on the hard disk, container name: {}", container.getName());
            throw new ResourceNotFoundException(container.getName());
        }

        try {
            container.setContent(Files.readAllBytes(containerPath));
            return container;
        } catch (IOException e) {
            log.error("Exception obtained during container read", e);
            throw new FileNotReadException(container.getName());
        }

    }

    private Container createContainer(FileSigner.SigningData signingData, String containerName) {
        try {
            Container container = new Container();
            container.setName(containerName);
            container.setContent(IOUtils.toByteArray(signingData.getContainer().saveAsStream()));
            container.setSignatureInHex(signingData.getSignatureInHex());
            return container;
        } catch (IOException e) {
            throw new RuntimeException("Error obtained during BdDoc container create", e);
        }
    }

    private String generateContainerName() {
        return RandomStringUtils.randomAlphabetic(10) + CONTAINER_FILE_EXTENSION;
    }


}
