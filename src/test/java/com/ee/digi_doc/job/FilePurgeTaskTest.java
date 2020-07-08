package com.ee.digi_doc.job;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class FilePurgeTaskTest {

    @Autowired
    private FileService fileService;

    @SpyBean
    private FilePurgeTask task;

    @Autowired
    private StorageProperties storageProperties;

    @BeforeEach
    public void before() {
        File file = fileService.create(FileGenerator.randomTxtFile());
        doReturn(List.of(file)).when(task).findEntitiesToCleanUp();
    }

    @AfterEach
    public void after() throws IOException {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath();
        FileUtils.cleanUp(filesDirectoryPath);
    }

    @Test
    void testTaskExecution() {
        await().atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> verify(task, atLeast(1)).cleanUp());
    }

}