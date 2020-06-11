package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SigningDataRestControllerTest extends AbstractRestControllerTest {

    @Test
    public void whenCreateSigningData_thenOk() throws Exception {
        assertSigningData(ok(createSigningData(createSigningDataRequest())));
    }

    @Test
    public void givenSigningDataExists_whenGet_thenOk() throws Exception {
        SigningDataDto expectedSigningData = retrieveSigningDataDto(ok(createSigningData(createSigningDataRequest())));
        assertSigningData(ok(getSigningData(expectedSigningData.getId())), expectedSigningData);
    }

    @Test
    public void givenSigningDataNotExists_whenGet_thenNotFound() throws Exception {
        Long notExistingSigningDataId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(getSigningData(notExistingSigningDataId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notExistingSigningDataId);
    }

    private ResultActions createFile(MockMultipartFile multipartFile) throws Exception {
        return multiPart("/files", multipartFile);
    }

    private ResultActions createSigningData(CreateSigningDataRequest request) throws Exception {
        return postJson("/signing-data", request);
    }

    private ResultActions getSigningData(@NotNull Long id) throws Exception {
        return get("/signing-data/" + id);
    }

    private void assertSigningData(ResultActions resultActions) throws Exception {
        resultActions.andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.containerName", is(notNullValue())))
                .andExpect(jsonPath("$.dataToSignName", is(notNullValue())))
                .andExpect(jsonPath("$.signatureInHex", is(notNullValue())));
    }

    private void assertSigningData(ResultActions resultActions, SigningDataDto expectedSigningData) throws Exception {
        resultActions.andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.containerName", is(expectedSigningData.getContainerName())))
                .andExpect(jsonPath("$.dataToSignName", is(expectedSigningData.getDataToSignName())))
                .andExpect(jsonPath("$.signatureInHex", is(expectedSigningData.getSignatureInHex())));
    }

    private CreateSigningDataRequest createSigningDataRequest() throws Exception {
        List<Long> fileIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            fileIds.add(getFileId(ok(createFile(FileGenerator.randomMultipartJpeg()))));
        }

        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(fileIds.toArray(Long[]::new));
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());
        return request;
    }

    private SigningDataDto retrieveSigningDataDto(ResultActions resultActions) throws Exception {
        byte[] content = resultActions.andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readValue(content, SigningDataDto.class);
    }

}