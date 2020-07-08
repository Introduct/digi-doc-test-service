package com.ee.digi_doc.storage.local;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.storage.StorageSigningDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Repository
public class LocalStorageSigningDataRepository implements StorageSigningDataRepository {

    private final Path signingDataStorageLocation;
    private final Configuration configuration;

    public LocalStorageSigningDataRepository(StorageProperties storageProperties, Digidoc4jProperties properties) {
        this.signingDataStorageLocation = Paths.get(storageProperties.getSigningData().getPath()).toAbsolutePath().normalize();
        this.configuration = new Configuration(properties.getMode());
    }

    @Override
    public void storeSigningData(SigningData signingData) {
        try {
            log.info("Store signing data: {}", signingData);

            String signingDataHash = getSigningDataHash(signingData);
            log.debug("Signing data hash: {}", signingData);

            Path signingDataDirectoryPath = Files.createDirectories(signingDataStorageLocation.resolve(signingDataHash));
            log.debug("Signing data directory path: {}", signingDataDirectoryPath);

            Path containerPath = signingDataDirectoryPath.resolve(signingData.getContainerName()).normalize();
            log.debug("Container of signing data path: {}", containerPath);

            Path dataToSignPath = signingDataDirectoryPath.resolve(signingData.getDataToSignName()).normalize();
            log.debug("ata to sign of signing data path: {}", containerPath);

            try (OutputStream outputStream = new FileOutputStream(containerPath.toFile())) {
                signingData.getContainer().save(outputStream);
                log.info("Container of signing data has been successfully stored");
            } catch (IOException e) {
                log.error("Error obtained during container write", e);
                throw new FileNotWrittenException(signingData.getId());
            }

            try (ObjectOutput objectOutput = new ObjectOutputStream(new FileOutputStream(dataToSignPath.toFile()))) {
                objectOutput.writeObject(signingData.getDataToSign());
                log.info("Data to sign of signing data has been successfully stored");
            } catch (IOException e) {
                log.error("Error obtained during data to sign write", e);
                throw new FileNotWrittenException(signingData.getId());
            }
        } catch (IOException e) {
            log.error("Error obtained during data to sign write", e);
            throw new FileNotWrittenException(signingData.getId());
        }
    }

    @Override
    public Container getContainer(SigningData signingData) {
        log.info("Get container of signing data: {}", signingData);

        Path containerPath = getSigningDataPath(signingData, signingData.getContainerName());
        log.debug("Container of signing data  path: {}", containerPath);

        try (InputStream inputStream = new FileInputStream(containerPath.toFile())) {
            Container container = BDocContainerBuilder.aContainer()
                    .fromStream(inputStream)
                    .withConfiguration(configuration)
                    .build();
            log.info("Container of signing data has been successfully read");
            return container;
        } catch (IOException e) {
            log.error("Error obtained during container read", e);
            throw new FileNotReadException(signingData.getId());
        }
    }

    @Override
    public DataToSign getDataToSign(SigningData signingData) {
        log.info("Get data to sign of signing data: {}", signingData);

        Path dataToSignPath = getSigningDataPath(signingData, signingData.getDataToSignName());
        log.debug("Data to sign of signing data  path: {}", dataToSignPath);

        try (ObjectInput objectInput = new ObjectInputStream(new FileInputStream(dataToSignPath.toFile()))) {
            DataToSign dataToSign = (DataToSign) objectInput.readObject();
            log.info("Data to sign of signing data has been successfully read");
            return dataToSign;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error obtained during data to sign read", e);
            throw new FileNotReadException(signingData.getId());
        }
    }

    @Override
    public void deleteContainer(SigningData signingData) {
        try {
            log.info("Delete container of signing data: {}", signingData);

            Path containerPath = getSigningDataPath(signingData, signingData.getContainerName());
            log.debug("Container of signing data  path: {}", containerPath);

            Files.delete(containerPath);
            log.info("Container of signing data has been successfully deleted");
        } catch (IOException e) {
            log.error("Error obtained during container delete: ", e);
            throw new FileNotDeletedException(signingData.getId());
        }
    }

    @Override
    public void deleteDataToSigh(SigningData signingData) {
        try {
            log.info("Delete data to sign of signing data: {}", signingData);

            Path dataToSignPath = getSigningDataPath(signingData, signingData.getDataToSignName());
            log.debug("Data to sign of signing data  path: {}", dataToSignPath);

            Files.delete(dataToSignPath);
            log.info("Data to sign of signing data has been successfully deleted");
        } catch (IOException e) {
            log.error("Error obtained during data to sign delete: ", e);
            throw new FileNotDeletedException(signingData.getId());
        }
    }

    private Path getSigningDataPath(SigningData signingData, String entityName) {
        String signingDataHash = getSigningDataHash(signingData);
        log.debug("Signing data hash: {}", signingData);

        Path signingDataDirectoryPath = signingDataStorageLocation.resolve(signingDataHash);
        log.debug("Signing data directory path: {}", signingDataDirectoryPath);

        return signingDataDirectoryPath.resolve(entityName).normalize();
    }

    public static String getSigningDataHash(SigningData signingData) {
        return Long.toHexString(signingData.getId());
    }
}
