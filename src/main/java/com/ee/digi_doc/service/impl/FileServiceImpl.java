package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.dao.FileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    public FileServiceImpl(FileRepository fileRepository, StorageProperties storageProperties) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        if (Files.notExists(fileStorageLocation)) {
            log.info("Directory {} does not exist", fileStorageLocation.toAbsolutePath());
            try {
                log.debug("Going to create directory {}", fileStorageLocation.toAbsolutePath());
                Files.createDirectories(fileStorageLocation);
                log.info("Directory {} has been created", fileStorageLocation.toAbsolutePath());
            } catch (IOException e) {
                log.error("Exception obtained during directory creation", e);
                throw new RuntimeException("Could not create directory for files", e);
            }
        }
    }

    @Override
    @Transactional
    public File create(MultipartFile multipartFile) {
        File file = fileRepository.saveAndFlush(File.of(multipartFile));
        log.info("Create file: {}", file);

        try {
            Files.write(fileStorageLocation.resolve(file.getName()), file.getContent());
            log.debug("File has been successfully written");
        } catch (IOException e) {
            log.error("Exception obtained during file write", e);
            throw new FileNotWrittenException(file.getName());
        }

        return file;
    }

    @Override
    public File get(@NotNull Long id) {
        log.info("Get file by id: {}", id);

        File file = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        log.debug("File has been found in database, file: {}", file);

        Path filePath = fileStorageLocation.resolve(file.getName()).normalize();
        log.debug("File path: {}", filePath.toAbsolutePath());

        if (Files.notExists(filePath)) {
            log.debug("File does not exist on the hard disk, file name: {}", file.getName());
            throw new ResourceNotFoundException(file.getName());
        }

        try {
            file.setContent(Files.readAllBytes(filePath));
            return file;
        } catch (IOException e) {
            log.error("Exception obtained during file read", e);
            throw new FileNotReadException(file.getName());
        }
    }

    @Override
    @Transactional
    public void delete(@NotNull Long id) {
        log.info("Delete file by id: {}", id);

        File file = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        log.debug("File has been found in database, file: {}", file);

        Path filePath = fileStorageLocation.resolve(file.getName()).normalize();
        log.debug("File path: {}", filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            log.debug("File does not exist on the hard disk, file name: {}", file.getName());
            throw new ResourceNotFoundException(file.getName());
        }

        fileRepository.delete(file);
        log.info("File has been deleted from database");

        try {
            Files.delete(filePath);
            log.info("File has been deleted from hard disk");
        } catch (IOException e) {
            log.error("Exception obtained during file delete", e);
            throw new FileNotDeletedException(file.getName());
        }
    }

}
