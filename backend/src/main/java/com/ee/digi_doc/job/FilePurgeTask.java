package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilePurgeTask extends AbstractPurgeTask<File> {

    private final JpaFileRepository jpaFileRepository;
    private final FileService fileService;

    @Override
    protected Collection<File> findEntitiesToCleanUp() {
        log.info("Find files to clean up");
        List<File> files = jpaFileRepository.findAllByUploadedOnBefore(LocalDateTime.now().minusDays(2));
        log.debug("Files to be cleaned up: {}", files);
        return files;
    }

    @Override
    protected void cleanUpEntity(File entity) {
        log.info("Clean up file: {}", entity);
        fileService.delete(entity);
        log.debug("File has been cleaned up");
    }
}
