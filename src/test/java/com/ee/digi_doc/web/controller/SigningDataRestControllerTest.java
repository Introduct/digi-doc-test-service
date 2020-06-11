package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.FileDto;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class SigningDataRestControllerTest {

    private static final String RESOURCE_NOT_FOUND_TEMPLATE = "Resource with id %s has not been found.";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenCreateSigningData_thenOk() throws Exception {
        assertSigningData(ok(createSigningData(createRequest())));
    }

    @Test
    public void givenSigningDataExists_whenGet_thenOk() throws Exception {
        SigningDataDto expectedSigningData = retrieveSigningDataDto(ok(createSigningData(createRequest())));
        assertSigningData(ok(getSigningData(expectedSigningData.getId())), expectedSigningData);
    }

    @Test
    public void givenSigningDataNotExists_whenGet_thenNotFound() throws Exception {
        Long notExistingSigningDataId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(getSigningData(notExistingSigningDataId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notExistingSigningDataId);
    }

    private ResultActions ok(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isOk());
    }

    private ResultActions notFound(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isNotFound());
    }

    private ResultActions createFile(MockMultipartFile multipartFile) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.multipart("/files").file(multipartFile));
    }

    private ResultActions createSigningData(CreateSigningDataRequest request) throws Exception {
        return mvc.perform(json(MockMvcRequestBuilders.post("/signing-data"), request));
    }

    private ResultActions getSigningData(@NotNull Long id) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get("/signing-data/" + id));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, Object request) throws Exception {
        return builder.content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON);
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

    private void assertErrorMessage(ResultActions resultActions, String errorMessageTemplate, Object argument)
            throws Exception {
        resultActions.andExpect(jsonPath("$.message", is(String.format(errorMessageTemplate, argument))));
    }

    private Long getFileId(ResultActions resultActions) throws Exception {
        byte[] content = resultActions.andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readValue(content, FileDto.class).getId();
    }

    private CreateSigningDataRequest createRequest() throws Exception {
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