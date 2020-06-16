package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SigningDataRestControllerTest extends AbstractRestControllerTest {

    @Test
    void whenCreateSigningData_thenOk() throws Exception {
        assertSigningData(ok(createSigningData(createSigningDataRequest())));
    }

    @Test
    void givenSigningDataExists_whenGet_thenOk() throws Exception {
        SigningDataDto expectedSigningData = retrieveSigningDataDto(ok(createSigningData(createSigningDataRequest())));
        assertSigningData(ok(getSigningData(expectedSigningData.getId())), expectedSigningData);
    }

    @Test
    void givenSigningDataNotExists_whenGet_thenNotFound() throws Exception {
        Long notExistingSigningDataId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(getSigningData(notExistingSigningDataId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notExistingSigningDataId);
    }

    @Test
    void givenFileIdsListNull_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setFileIds(null);

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty", "fileIds",
                "must not be empty");
    }

    @Test
    void givenFileIdsListEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setFileIds(new ArrayList<>());

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty", "fileIds",
                "must not be empty");
    }

    @Test
    void givenCertificateInHexNull_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex(null);

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty", "certificateInHex",
                "must not be empty");
    }

    @Test
    void givenCertificateInHexEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex("");

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty", "certificateInHex",
                "must not be empty");
    }

    @Test
    void givenNotAllFileExist_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.getFileIds().add(Long.valueOf(randomNumeric(3)));
        assertFieldError(badRequest(createSigningData(request)), "ValidFileIds", "fileIds",
                "Not all files are found by provided file ids.");
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

}