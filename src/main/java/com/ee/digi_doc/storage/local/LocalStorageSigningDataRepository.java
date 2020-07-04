package com.ee.digi_doc.storage.local;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.storage.StorageSigningDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class LocalStorageSigningDataRepository implements StorageSigningDataRepository {

    private final Path signingDataStorageLocation;
    private final Configuration configuration;

    public LocalStorageSigningDataRepository(StorageProperties storageProperties, Digidoc4jProperties properties) {
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
        Path containerPath = signingDataStorageLocation.resolve(getUniqueContainerName(signingData)).normalize();
        Path dataToSignPath = signingDataStorageLocation.resolve(getUniqueDataToSignName(signingData)).normalize();

        try (OutputStream outputStream = new FileOutputStream(containerPath.toFile())) {
            signingData.getContainer().save(outputStream);
        } catch (IOException e) {
            log.error("Error obtained during container write", e);
            throw new FileNotWrittenException(getUniqueContainerName(signingData));
        }

        try (ObjectOutput objectOutput = new ObjectOutputStream(new FileOutputStream(dataToSignPath.toFile()))) {
            objectOutput.writeObject(signingData.getDataToSign());
        } catch (IOException e) {
            log.error("Error obtained during data to sign write", e);
            throw new FileNotWrittenException(getUniqueDataToSignName(signingData));
        }
    }

    @Override
    public Container getContainer(SigningData signingData) {
        Path containerPath = signingDataStorageLocation.resolve(getUniqueContainerName(signingData)).normalize();
        try (InputStream inputStream = new FileInputStream(containerPath.toFile())) {
            return BDocContainerBuilder.aContainer().fromStream(inputStream).withConfiguration(configuration).build();
        } catch (IOException e) {
            log.error("Error obtained during container read", e);
            throw new FileNotReadException(getUniqueContainerName(signingData));
        }
    }

    @Override
    public DataToSign getDataToSign(SigningData signingData) {
        Path dataToSignPath = signingDataStorageLocation.resolve(getUniqueDataToSignName(signingData)).normalize();
        try (ObjectInput objectInput = new ObjectInputStream(new FileInputStream(dataToSignPath.toFile()))) {
            return (DataToSign) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error obtained during data to sign read", e);
            throw new FileNotReadException(getUniqueDataToSignName(signingData));
        }
    }

    @Override
    public void deleteContainer(SigningData signingData) {
        try {
            Path containerPath = signingDataStorageLocation.resolve(getUniqueContainerName(signingData)).normalize();
            Files.delete(containerPath);
        } catch (IOException e) {
            log.error("Error obtained during container delete: " + getUniqueContainerName(signingData), e);
            throw new FileNotDeletedException(getUniqueContainerName(signingData));
        }
    }

    @Override
    public void deleteDataToSigh(SigningData signingData) {
        try {
            Path dataToSignPath = signingDataStorageLocation.resolve(getUniqueDataToSignName(signingData)).normalize();
            Files.delete(dataToSignPath);
        } catch (IOException e) {
            log.error("Error obtained during data to sign delete: " + getUniqueDataToSignName(signingData), e);
            throw new FileNotDeletedException(getUniqueDataToSignName(signingData));
        }
    }

    public static String getUniqueContainerName(SigningData signingData) {
        return StringUtils.join(new Object[]{signingData.getId(), signingData.getContainerName()}, "-");
    }

    public static String getUniqueDataToSignName(SigningData signingData) {
        return StringUtils.join(new Object[]{signingData.getId(), signingData.getDataToSignName()}, "-");
    }
}
