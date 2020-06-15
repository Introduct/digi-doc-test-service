package com.ee.digi_doc.job;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
class FilePurgeTaskTest {

    @SpyBean
    private FilePurgeTask task;

    @Test
    void testTaskExecution() {
        await().atMost(Duration.ONE_MINUTE)
                .untilAsserted(() -> verify(task, atLeast(1)).cleanUp());
    }

}