package com.ee.digi_doc.storage.impl;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.storage.LocalStorageFileRepository;
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
public class LocalStorageFileRepositoryImpl implements LocalStorageFileRepository {

    private final Path fileStorageLocation;

    public LocalStorageFileRepositoryImpl(StorageProperties storageProperties) {
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
            Files.write(fileStorageLocation.resolve(file.getName()), file.getContent());
        } catch (IOException e) {
            log.error("Exception obtained during file write", e);
            throw new FileNotWrittenException(file.getName());
        }
    }

    @Override
    public Optional<byte[]> getFileContent(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            return Files.exists(filePath) ? Optional.of(Files.readAllBytes(filePath)) : Optional.empty();
        } catch (IOException e) {
            log.error("Exception obtained during file read from local storage", e);
            throw new FileNotReadException(fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException(fileName);
            }
            Files.delete(filePath);
        } catch (IOException e) {
            log.error("Exception obtained during file delete from local storage", e);
            throw new FileNotDeletedException(fileName);
        }
    }
}
