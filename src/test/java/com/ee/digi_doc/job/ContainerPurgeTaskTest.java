package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.ContainerService;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.ee.digi_doc.web.request.SignContainerRequest;
import org.digidoc4j.DigestAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ContainerPurgeTaskTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private SigningDataService signingDataService;

    @Autowired
    private ContainerService containerService;

    @SpyBean
    private ContainerPurgeTask task;

    @BeforeEach
    public void before() {
        CreateSigningDataRequest createDataToSignRequest = new CreateSigningDataRequest();
        createDataToSignRequest.setFileIds(List.of(fileService.create(FileGenerator.randomTxtFile()).getId()));
        createDataToSignRequest.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData signingData = signingDataService.create(createDataToSignRequest);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);

        Container container = containerService.signContainer(signContainerRequest);

        doReturn(List.of(container)).when(task).findEntitiesToCleanUp();
    }

    @Test
    void testTaskExecution() {
        await().atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> verify(task, atLeast(1)).cleanUp());
    }

}