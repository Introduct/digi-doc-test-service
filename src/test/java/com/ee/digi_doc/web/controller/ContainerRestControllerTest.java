package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.SigningDataService;
import com.ee.digi_doc.util.FileUtils;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.ContainerDto;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.ee.digi_doc.web.request.SignContainerRequest;
import org.digidoc4j.DigestAlgorithm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ee.digi_doc.util.FileGenerator.randomFile;
import static com.ee.digi_doc.util.FileGenerator.randomTxtFile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContainerRestControllerTest extends AbstractRestControllerTest {

    @Autowired
    private SigningDataService signingDataService;

    @Autowired
    private JpaContainerRepository jpaContainerRepository;

    private static StorageProperties storageProperties;

    @Autowired
    public void setStorageProperties(StorageProperties storageProperties) {
        ContainerRestControllerTest.storageProperties = storageProperties;
    }

    @AfterAll
    public static void after() throws IOException {
        Path containerDirectoryPath = Paths.get(storageProperties.getContainer().getPath());
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath());
        Path fileDirectoryPath = Paths.get(storageProperties.getFile().getPath());

        FileUtils.cleanUp(containerDirectoryPath);
        FileUtils.cleanUp(signingDataDirectoryPath);
        FileUtils.cleanUp(fileDirectoryPath);
    }

    @Test
    void whenSignContainer_thenOk() throws Exception {
        assertContainer(ok(signContainer(createSignContainerRequest())));
    }

    @Test
    void givenFileHasEmptyContentType_whenSignContainer_thenOk() throws Exception {
        List<Long> fileIds = new ArrayList<>();

        fileIds.add(getFileId(ok(createFile(randomTxtFile()))));
        fileIds.add(getFileId(ok(createFile(randomFile(randomAlphabetic(10), 10, null)))));

        assertContainer(ok(signContainer(createSignContainerRequest(createSigningDataRequest(fileIds)))));
    }

    @Test
    void givenContainerSigned_whenGet_thenOk() throws Exception {
        ContainerDto containerDto = retrieveContainerDto(ok(signContainer(createSignContainerRequest())));
        ok(getContainer(containerDto.getId()));
    }

    @Test
    void givenSigningDataNotExists_whenSignContainer_thenNotFound() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSigningDataId(getNotExistingSigningDataId());
        assertErrorMessage(notFound(signContainer(request)), RESOURCE_NOT_FOUND_TEMPLATE, request.getSigningDataId());
    }

    @Test
    void givenContainerNotSigned_whenGet_thenNotFound() throws Exception {
        Long notSignedContainerId = getNotExistingContainerId();
        assertErrorMessage(notFound(getContainer(notSignedContainerId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notSignedContainerId);
    }

    @Test
    void givenContainerNotSigned_whenValidate_thenNotFound() throws Exception {
        Long notSignedContainerId = getNotExistingContainerId();
        assertErrorMessage(notFound(validateContainer(notSignedContainerId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notSignedContainerId);
    }

    @Test
    void givenContainerSigned_whenValidate_thenOk() throws Exception {
        ContainerDto containerDto = retrieveContainerDto(ok(signContainer(createSignContainerRequest())));
        assertValidateResult(ok(validateContainer(containerDto.getId())));
    }

    @Test
    void givenContainerSigned_whenGenerateLink_thenOK() throws Exception {
        ContainerDto containerDto = retrieveContainerDto(ok(signContainer(createSignContainerRequest())));
        generateLink(containerDto.getId())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.link", is(notNullValue())));
    }

    @Test
    void givenContainerNotSigned_whenGenerateLink_thenNotFound() throws Exception {
        long notExistingContainerId = getNotExistingContainerId();
        assertErrorMessage(notFound(generateLink(notExistingContainerId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notExistingContainerId);
    }

    @Test
    void givenSigningDataIdNull_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSigningDataId(null);

        assertFieldError(badRequest(signContainer(request)), "NotNull", "signingDataId",
                MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenSignatureInHexNull_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSignatureInHex(null);

        assertFieldError(badRequest(signContainer(request)), "NotEmpty", "signatureInHex",
                MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenSignatureInHexEmpty_whenSignContainer_thenBadRequest() throws Exception {
        SignContainerRequest request = createSignContainerRequest();
        request.setSignatureInHex("");

        assertFieldError(badRequest(signContainer(request)), "NotEmpty", "signatureInHex",
                MUST_NOT_BE_EMPTY_TEMPLATE);
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

    private ResultActions generateLink(@NotNull Long id) throws Exception {
        return get("/containers/" + id + "/link");
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
        return createSignContainerRequest(createSigningDataRequest());
    }

    private SignContainerRequest createSignContainerRequest(CreateSigningDataRequest request) throws Exception {
        SigningDataDto signingDataDto = retrieveSigningDataDto(ok(createSigningData(request)));

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

    private long getNotExistingSigningDataId() {
        long notExistingSigningDataId = Long.parseLong(randomNumeric(3));

        while (signingDataService.getSigningData(notExistingSigningDataId).isPresent()) {
            notExistingSigningDataId = Long.parseLong(randomNumeric(3));
        }

        return notExistingSigningDataId;
    }

    private long getNotExistingContainerId() {
        long notExistingContainerId = Long.parseLong(randomNumeric(3));

        while (jpaContainerRepository.findById(notExistingContainerId).isPresent()) {
            notExistingContainerId = Long.parseLong(randomNumeric(3));
        }

        return notExistingContainerId;
    }

}