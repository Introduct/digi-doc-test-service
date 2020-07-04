package com.ee.digi_doc.storage.local;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.storage.StorageFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Repository
public class LocalStorageFileRepository implements StorageFileRepository {

    private final Path fileStorageLocation;

    public LocalStorageFileRepository(StorageProperties storageProperties) {
        this.fileStorageLocation = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        if (Files.notExists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory for files", e);
            }
        }
    }

    @Override
    public void storeFile(File file) {
        try {
            Files.write(fileStorageLocation.resolve(getUniqueFileName(file)), file.getContent());
        } catch (IOException e) {
            log.error("Exception obtained during file write", e);
            throw new FileNotWrittenException(getUniqueFileName(file));
        }
    }

    @Override
    public Optional<byte[]> getFileContent(File file) {
        try {
            Path filePath = fileStorageLocation.resolve(getUniqueFileName(file)).normalize();
            return Files.exists(filePath) ? Optional.of(Files.readAllBytes(filePath)) : Optional.empty();
        } catch (IOException e) {
            log.error("Exception obtained during file read from local storage", e);
            throw new FileNotReadException(getUniqueFileName(file));
        }
    }

    @Override
    public void deleteFile(File file) {
        try {
            Path filePath = fileStorageLocation.resolve(getUniqueFileName(file)).normalize();

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException(getUniqueFileName(file));
            }
            Files.delete(filePath);
        } catch (IOException e) {
            log.error("Exception obtained during file delete from local storage", e);
            throw new FileNotDeletedException(getUniqueFileName(file));
        }
    }

    public static String getUniqueFileName(File file) {
        return org.apache.commons.lang3.StringUtils.join(new Object[]{file.getId(), file.getName()}, "-");
    }
}
