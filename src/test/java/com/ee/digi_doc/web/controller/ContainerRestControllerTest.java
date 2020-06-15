package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.ContainerDto;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.SignContainerRequest;
import org.digidoc4j.DigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ContainerRestControllerTest extends AbstractRestControllerTest {

    @Autowired
    private SigningDataService signingDataService;

    @Test
    void whenSignContainer_thenOk() throws Exception {
        assertContainer(ok(signContainer(createSignContainerRequest())));
    }

    @Test
    void givenContainerSigned_whenGet_thenOk() throws Exception {
        ContainerDto containerDto = retrieveContainerDto(ok(signContainer(createSignContainerRequest())));
        ok(getContainer(containerDto.getId()));
    }

    @Test
    void givenSigningDataNotExists_whenSignContainer_thenNotFound() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSigningDataId(Long.valueOf(randomNumeric(3)));
        assertErrorMessage(notFound(signContainer(request)), RESOURCE_NOT_FOUND_TEMPLATE, request.getSigningDataId());
    }

    @Test
    void givenContainerNotSigned_whenGet_thenNotFound() throws Exception {
        Long notSignedContainerId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(getContainer(notSignedContainerId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notSignedContainerId);
    }

    @Test
    void givenContainerNotSigned_whenValidate_thenNotFound() throws Exception {
        Long notSignedContainerId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(validateContainer(notSignedContainerId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notSignedContainerId);
    }

    @Test
    void givenContainerSigned_whenValidate_thenOk() throws Exception {
        ContainerDto containerDto = retrieveContainerDto(ok(signContainer(createSignContainerRequest())));
        assertValidateResult(ok(validateContainer(containerDto.getId())));
    }

    @Test
    void givenSigningDataIdNull_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSigningDataId(null);

        assertFieldError(badRequest(signContainer(request)), "NotNull", "signingDataId",
                "must not be null");
    }

    @Test
    void givenSignatureInHexNull_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSignatureInHex(null);

        assertFieldError(badRequest(signContainer(request)), "NotEmpty", "signatureInHex",
                "must not be empty");
    }

    @Test
    void givenSignatureInHexEmpty_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSignatureInHex("");

        assertFieldError(badRequest(signContainer(request)), "NotEmpty", "signatureInHex",
                "must not be empty");
    }

    private ResultActions signContainer(@NotNull SignContainerRequest request) throws Exception {
        return postJson("/containers", request);
    }

    private ResultActions getContainer(@NotNull Long id) throws Exception {
        return get("/containers/" + id);
    }

    private ResultActions validateContainer(@NotNull Long id) throws Exception {
        return get("/containers/" + id + "/validate");
    }

    private void assertContainer(ResultActions resultActions) throws Exception {
        resultActions.andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(notNullValue())))
                .andExpect(jsonPath("$.signedOn", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(startsWith("/api/v1/containers/"))));
    }

    private void assertValidateResult(ResultActions resultActions) throws Exception {
        resultActions.andExpect(jsonPath("$.valid", is(notNullValue())))
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.signerIdCode", is(notNullValue())))
                .andExpect(jsonPath("$.signerFirstName", is(notNullValue())))
                .andExpect(jsonPath("$.signerLastName", is(notNullValue())))
                .andExpect(jsonPath("$.signerCountryCode", is(notNullValue())))
                .andExpect(jsonPath("$.signedOn", is(notNullValue())));
    }

    private SignContainerRequest createSignContainerRequest() throws Exception {
        SigningDataDto signingDataDto = retrieveSigningDataDto(ok(createSigningData(createSigningDataRequest())));

        SigningData signingData = signingDataService.getSigningData(signingDataDto.getId()).orElse(null);
        assertNotNull(signingData);

        String signatureInHex = TestSigningData.rsaSignData(signingData.getDataToSign(), DigestAlgorithm.SHA256);

        SignContainerRequest signContainerRequest = new SignContainerRequest();
        signContainerRequest.setSigningDataId(signingData.getId());
        signContainerRequest.setSignatureInHex(signatureInHex);
        return signContainerRequest;
    }

    private ContainerDto retrieveContainerDto(ResultActions resultActions) throws Exception {
        byte[] content = resultActions.andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readValue(content, ContainerDto.class);
    }

}