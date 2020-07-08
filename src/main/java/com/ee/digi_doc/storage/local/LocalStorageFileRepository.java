package com.ee.digi_doc.storage.local;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.storage.StorageFileRepository;
import com.ee.digi_doc.storage.local.util.HexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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

    @Override
    public void storeFile(File file) {
        try {
            log.info("Store file: {}", file);

            String fileHash = HexUtils.getFileHex(file);
            log.debug("File hash: {}", fileHash);

            Path fileDirectoryPath = Files.createDirectories(fileStorageLocation.resolve(fileHash));
            log.debug("File directory path: {}", fileDirectoryPath);

            Path filePath = fileDirectoryPath.resolve(file.getName()).normalize();
            log.debug("File path: {}", filePath.toString());

            Files.write(filePath, file.getContent());
            log.info("File has been successfully stored");
        } catch (IOException e) {
            log.error("Exception obtained during file write", e);
            throw new FileNotWrittenException(file.getName());
        }
    }

    @Override
    public Optional<byte[]> getFileContent(File file) {
        try {
            log.info("Get file content, file: {}", file);

            Path filePath = getExistingFilePath(file);
            log.debug("File path: {}", filePath.toString());

            if (Files.exists(filePath)) {
                log.debug("File content has been found");
                byte[] content = Files.readAllBytes(filePath);
                log.info("File content has been successfully read");
                return Optional.of(content);
            } else {
                log.debug("File content has not been found");
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("Exception obtained during file read from local storage", e);
            throw new FileNotReadException(file.getName());
        }
    }

    @Override
    public void deleteFile(File file) {
        try {
            log.info("Delete file: {}", file);

            Path filePath = getExistingFilePath(file);
            log.debug("File path: {}", filePath.toString());

            if (!Files.exists(filePath)) {
                log.debug("File content has not been found");
                throw new ResourceNotFoundException(file.getName());
            }
            Files.delete(filePath);
            log.info("File has been successfully deleted");
        } catch (IOException e) {
            log.error("Exception obtained during file delete from local storage", e);
            throw new FileNotDeletedException(file.getName());
        }
    }

    private Path getExistingFilePath(File file) {
        String fileHash = HexUtils.getFileHex(file);
        log.debug("File hash: {}", fileHash);

        Path fileDirectoryPath = fileStorageLocation.resolve(fileHash);
        log.debug("File directory path: {}", fileDirectoryPath);

        return fileDirectoryPath.resolve(file.getName()).normalize();
    }

}