package com.ee.digi_doc.storage.impl;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.storage.LocalStorageSigningDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Repository
public class LocalStorageSigningDataRepositoryImpl implements LocalStorageSigningDataRepository {

    private final Path signingDataStorageLocation;
    private final Configuration configuration;

    public LocalStorageSigningDataRepositoryImpl(StorageProperties storageProperties, Digidoc4jProperties properties) {
        this.signingDataStorageLocation = Paths.get(storageProperties.getSigningData().getPath()).toAbsolutePath().normalize();
        this.configuration = new Configuration(properties.getMode());
    }

    @PostConstruct
    public void init() {
        if (Files.notExists(signingDataStorageLocation)) {
            try {
                Files.createDirectories(signingDataStorageLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory for signing data", e);
            }
        }
    }

    @Override
    public void storeSigningData(SigningData signingData) {
        Path containerPath = signingDataStorageLocation.resolve(signingData.getContainerName()).normalize();
        Path dataToSignPath = signingDataStorageLocation.resolve(signingData.getDataToSignName()).normalize();

        try (OutputStream outputStream = new FileOutputStream(containerPath.toFile())) {
            signingData.getContainer().save(outputStream);
        } catch (IOException e) {
            log.error("Error obtained during container write", e);
            throw new FileNotWrittenException(signingData.getContainerName());
        }

        try (ObjectOutput objectOutput = new ObjectOutputStream(new FileOutputStream(dataToSignPath.toFile()))) {
            objectOutput.writeObject(signingData.getDataToSign());
        } catch (IOException e) {
            log.error("Error obtained during data to sign write", e);
            throw new FileNotWrittenException(signingData.getDataToSignName());
        }
    }

    @Override
    public Container getContainer(String containerName) {
        Path containerPath = signingDataStorageLocation.resolve(containerName).normalize();
        try (InputStream inputStream = new FileInputStream(containerPath.toFile())) {
            return BDocContainerBuilder.aContainer().fromStream(inputStream).withConfiguration(configuration).build();
        } catch (IOException e) {
            log.error("Error obtained during container read", e);
            throw new FileNotReadException(containerName);
        }
    }

    @Override
    public DataToSign getDataToSign(String dataToSignName) {
        Path dataToSignPath = signingDataStorageLocation.resolve(dataToSignName).normalize();
        try (ObjectInput objectInput = new ObjectInputStream(new FileInputStream(dataToSignPath.toFile()))) {
            return (DataToSign) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error obtained during data to sign read", e);
            throw new FileNotReadException(dataToSignName);
        }
    }

    @Override
    public void deleteContainer(String containerName) {
        try {
            Path containerPath = signingDataStorageLocation.resolve(containerName).normalize();
            Files.delete(containerPath);
        } catch (IOException e) {
            log.error("Error obtained during container delete: " + containerName, e);
            throw new FileNotDeletedException(containerName);
        }
    }

    @Override
    public void deleteDataToSigh(String dataToSignName) {
        try {
            Path dataToSignPath = signingDataStorageLocation.resolve(dataToSignName).normalize();
            Files.delete(dataToSignPath);
        } catch (IOException e) {
            log.error("Error obtained during data to sign delete: " + dataToSignName, e);
            throw new FileNotDeletedException(dataToSignName);
        }
    }
}
