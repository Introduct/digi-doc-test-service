package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.apache.pdfbox.pdmodel.common.filespecification.PDSimpleFileSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SigningDataRestControllerTest extends AbstractRestControllerTest {

    public static final String MAX_FILE_COUNT_EXCEEDED_TEMPLATE = "Max file count to sign is %s.";
    public static final String EMPTY_CERTIFICATE_IN_HEX_TEMPLATE = "Certificate in hex key is required.";
    public static final String EMPTY_FILE_IDS_TEMPLATE = "File ids key is required.";
    public static final String NOT_ALL_FILES_FOUNT_TEMPLATE = "Not all files are found by provided file ids.";

    @Value("${sign.max-file-count}")
    private int maxFileCount;

    @Autowired
    private JpaFileRepository jpaFileRepository;

    @Autowired
    private JpaSigningDataRepository jpaSigningDataRepository;

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
        Long notExistingSigningDataId = getNotExistingSigningDataId();
        assertErrorMessage(notFound(getSigningData(notExistingSigningDataId)), RESOURCE_NOT_FOUND_TEMPLATE,
                notExistingSigningDataId);
    }

    @Test
    void givenFileIdsListNull_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setFileIds(null);

        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.EmptyFileIds",
                "fileIds", EMPTY_FILE_IDS_TEMPLATE);
    }

    @Test
    void givenFileIdsListEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setFileIds(new ArrayList<>());

        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.EmptyFileIds",
                "fileIds", EMPTY_FILE_IDS_TEMPLATE);
    }

    @Test
    void givenCertificateInHexNull_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex(null);

        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.EmptyCertificateInHex",
                "certificateInHex", EMPTY_CERTIFICATE_IN_HEX_TEMPLATE);
    }

    @Test
    void givenCertificateInHexEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex("");

        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.EmptyCertificateInHex",
                "certificateInHex", EMPTY_CERTIFICATE_IN_HEX_TEMPLATE);
    }

    @Test
    void givenNotAllFileExist_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.getFileIds().add(getNotExistingFileId());
        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.NotAllFilesExist",
                "fileIds", NOT_ALL_FILES_FOUNT_TEMPLATE);
    }

    @Test
    void givenFileCountLargerThenMax_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomFile()))));
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomFile()))));
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomFile()))));
        assertFieldError(badRequest(createSigningData(request)), "Validation.SigningData.MaxFileCount",
                "fileIds", MAX_FILE_COUNT_EXCEEDED_TEMPLATE, maxFileCount);
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

    private Long getNotExistingFileId() {
        long notExistingFileId = Long.parseLong(randomNumeric(3));

        while (jpaFileRepository.findById(notExistingFileId).isPresent()) {
            notExistingFileId = Long.parseLong(randomNumeric(3));
        }

        return notExistingFileId;
    }

    private Long getNotExistingSigningDataId() {
        long notExistingSigningDataId = Long.parseLong(randomNumeric(3));

        while (jpaSigningDataRepository.findById(notExistingSigningDataId).isPresent()) {
            notExistingSigningDataId = Long.parseLong(randomNumeric(3));
        }

        return notExistingSigningDataId;
    }

}