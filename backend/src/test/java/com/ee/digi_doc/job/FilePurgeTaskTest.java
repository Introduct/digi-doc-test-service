package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.util.FileGenerator;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

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

    @BeforeEach
    public void before() {
        File file = fileService.create(FileGenerator.randomMultipartJpeg());
        doReturn(List.of(file)).when(task).findEntitiesToCleanUp();
    }

    @Test
    void testTaskExecution() {
        await().atMost(Duration.ONE_MINUTE)
                .untilAsserted(() -> verify(task, atLeast(1)).cleanUp());
    }

}