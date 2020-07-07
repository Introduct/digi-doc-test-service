package com.ee.digi_doc.mapper;

import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class SigningDataMapperTest {

    @Value("${test.file.number:10}")
    private int fileNumber;

    @Autowired
    private SigningDataMapper mapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private SigningDataService signingDataService;

    @Test
    void toDtoTest() {
        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(createFiles());
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());

        SigningData source = signingDataService.create(request);

        SigningDataDto target = mapper.toDto(source);

        assertNotNull(target);
        assertNotNull(target.getId());
        assertNotNull(target.getContainerName());
        assertNotNull(target.getDataToSignName());
        assertNotNull(target.getSignatureInHex());

        assertEquals(source.getId(), target.getId());
        assertEquals(source.getContainerName(), target.getContainerName());
        assertEquals(source.getDataToSignName(), target.getDataToSignName());
        assertEquals(source.getSignatureInHex(), target.getSignatureInHex());
    }

    @Test
    void givenInputNull_whenToDto_thenOutputNull() {
        assertNull(mapper.toDto(null));
    }

    private List<Long> createFiles() {
        List<Long> fileIds = new ArrayList<>();
        for (int i = 0; i < fileNumber; i++) {
            fileIds.add(fileService.create(FileGenerator.randomTxtFile()).getId());
        }
        return fileIds;
    }

}