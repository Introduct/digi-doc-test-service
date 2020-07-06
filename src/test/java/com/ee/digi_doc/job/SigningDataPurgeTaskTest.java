package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
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
class SigningDataPurgeTaskTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private SigningDataService signingDataService;

    @SpyBean
    private SigningDataPurgeTask task;

    @BeforeEach
    public void before() {
        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(List.of(fileService.create(FileGenerator.randomTxtFile()).getId()));
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());
        SigningData signingData = signingDataService.create(request);
        doReturn(List.of(signingData)).when(task).findEntitiesToCleanUp();
    }

    @Test
    void testTaskExecution() {
        await().atMost(Duration.ONE_MINUTE)
                .untilAsserted(() -> verify(task, atLeast(1)).cleanUp());
    }

}