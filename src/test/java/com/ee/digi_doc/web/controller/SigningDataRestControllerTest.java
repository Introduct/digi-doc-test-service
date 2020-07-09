package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.FileUtils;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class SigningDataRestControllerTest extends AbstractRestControllerTest {

    private static final String MAX_FILE_COUNT_EXCEEDED_TEMPLATE = "Max file count is exceeded.";
    private static final String NOT_ALL_FILES_FOUNT_TEMPLATE = "Not all files are found by provided file ids.";
    private static final String DUPLICATE_FILES_TEMPLATE = "There file duplicates.";

    @Value("${sign.max-file-count}")
    private int maxFileCount;

    @Autowired
    private JpaFileRepository jpaFileRepository;

    @Autowired
    private JpaSigningDataRepository jpaSigningDataRepository;

    private static StorageProperties storageProperties;

    @Autowired
    public void setStorageProperties(StorageProperties storageProperties) {
        SigningDataRestControllerTest.storageProperties = storageProperties;
    }

    @AfterAll
    public static void after() throws IOException {
        Path signingDataDirectoryPath = Paths.get(storageProperties.getSigningData().getPath());
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath());

        FileUtils.cleanUp(signingDataDirectoryPath);
        FileUtils.cleanUp(filesDirectoryPath);
    }

    @Test
    void whenCreateSigningData_thenOk() throws Exception {
        assertSigningData(ok(createSigningData(createSigningDataRequest())));
    }

    @Test
    void givenFileHasEmptyContentType_whenCreateSigningData_thenOk() throws Exception {
        List<Long> fileIds = new ArrayList<>();

        fileIds.add(getFileId(ok(createFile(randomTxtFile()))));
        fileIds.add(getFileId(ok(createFile(randomFile(randomAlphabetic(10), 10, null)))));

        assertSigningData(ok(createSigningData(createSigningDataRequest(fileIds))));
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

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty",
                "fileIds", MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenFileIdsListEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setFileIds(new ArrayList<>());

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty",
                "fileIds", MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenCertificateInHexNull_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex(null);

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty",
                "certificateInHex", MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenCertificateInHexEmpty_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.setCertificateInHex("");

        assertFieldError(badRequest(createSigningData(request)), "NotEmpty",
                "certificateInHex", MUST_NOT_BE_EMPTY_TEMPLATE);
    }

    @Test
    void givenNotAllFileExist_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.getFileIds().set(0, getNotExistingFileId());
        assertFieldError(badRequest(createSigningData(request)), "ValidExistingFileIds",
                "fileIds", NOT_ALL_FILES_FOUNT_TEMPLATE);
    }

    @Test
    void givenFileCountLargerThenMax_whenCreateSigningData_thenBadRequest() throws Exception {
        CreateSigningDataRequest request = createSigningDataRequest();
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomTxtFile()))));
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomTxtFile()))));
        request.getFileIds().add(getFileId(ok(createFile(FileGenerator.randomTxtFile()))));
        assertFieldError(badRequest(createSigningData(request)), "ValidFileIdsCount",
                "fileIds", MAX_FILE_COUNT_EXCEEDED_TEMPLATE, maxFileCount);
    }

    @Test
    void givenFileUploadedTwice_thenCreateSigningData_badRequest() throws Exception {
        MockMultipartFile multipartFile = randomTxtFile();

        List<Long> fileIds = List.of(getFileId(ok(createFile(multipartFile))),
                getFileId(ok(createFile(multipartFile))));

        CreateSigningDataRequest request = createSigningDataRequest(fileIds);

        assertFieldError(badRequest(createSigningData(request)), "ValidUniqueFiles",
                "fileIds", DUPLICATE_FILES_TEMPLATE);
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