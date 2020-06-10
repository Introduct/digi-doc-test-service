package com.ee.digi_doc.storage.impl;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.storage.LocalStorageContainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Configuration;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Repository
public class LocalStorageContainerRepositoryImpl implements LocalStorageContainerRepository {

    private final Path signingDataStorageLocation;
    private final Configuration configuration;

    public LocalStorageContainerRepositoryImpl(StorageProperties storageProperties, Digidoc4jProperties properties) {
        this.signingDataStorageLocation = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();
        this.configuration = new Configuration(properties.getMode());
    }

    @PostConstruct
    public void init() {
        if (Files.notExists(signingDataStorageLocation)) {
            try {
                Files.createDirectories(signingDataStorageLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory for container", e);
            }
        }
    }

    @Override
    public void storeContainer(Container container) {
        Path containerPath = signingDataStorageLocation.resolve(container.getName()).normalize();
        try (OutputStream outputStream = new FileOutputStream(containerPath.toFile())) {
            container.getBdDocContainer().save(outputStream);
        } catch (IOException e) {
            log.error("Error obtained during container write", e);
            throw new FileNotWrittenException(container.getName());
        }
    }

    @Override
    public org.digidoc4j.Container getContainer(String containerName) {
        Path containerPath = signingDataStorageLocation.resolve(containerName).normalize();
        try (InputStream inputStream = new FileInputStream(containerPath.toFile())) {
            return BDocContainerBuilder.aContainer().fromStream(inputStream).withConfiguration(configuration).build();
        } catch (IOException e) {
            log.error("Error obtained during container read", e);
            throw new FileNotReadException(containerName);
        }
    }
}
