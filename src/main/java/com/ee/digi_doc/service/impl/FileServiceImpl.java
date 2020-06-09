package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.storage.LocalStorageFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final JpaFileRepository jpaFileRepository;
    private final LocalStorageFileRepository localStorageFileRepository;

    @Override
    @Transactional
    public File create(MultipartFile multipartFile) {
        log.info("Create file with name {} and content type {}", multipartFile.getOriginalFilename(),
                multipartFile.getContentType());

        File file = jpaFileRepository.saveAndFlush(File.of(multipartFile));
        log.debug("File has been successfully created in database, file: {}", file);

        localStorageFileRepository.storeFile(file);
        log.debug("File has been successfully saved in local storage, file: {}", file);

        return file;
    }

    @Override
    public Optional<File> get(@NotNull Long id) {
        log.info("Get file by id: {}", id);
        return jpaFileRepository.findById(id)
                .flatMap(file -> {
                    log.debug("File has been found in database, file: {}", file);
                    return localStorageFileRepository.getFileContent(file.getName())
                            .map(bytes -> {
                                log.debug("File content has been found in local storage");
                                file.setContent(bytes);
                                return file;
                            });
                });
    }

    @Override
    @Transactional
    public void delete(@NotNull Long id) {
        log.info("Delete file by id: {}", id);
        jpaFileRepository.findById(id)
                .ifPresent(file -> {
                    jpaFileRepository.delete(file);
                    log.info("File has been deleted from database");
                    localStorageFileRepository.deleteFile(file.getName());
                    log.info("File has been deleted from local storage");
                });
    }

}
