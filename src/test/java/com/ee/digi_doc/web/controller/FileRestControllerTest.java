package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.common.properties.FileUploadProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.ee.digi_doc.util.FileGenerator.randomFile;
import static com.ee.digi_doc.util.FileGenerator.randomTxtFile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FileRestControllerTest extends AbstractRestControllerTest {

    private static final String INVALID_FILE_NAME_TEMPLATE = "Invalid file name %s.";
    private static final String FILE_EMPTY_REQUEST_TEMPLATE = "File key is required.";
    private static final String MAX_FILE_NAME_TEMPLATE =
            "The file %s exceeds its maximum permitted file name of %s characters.";
    private static final String MAX_FILE_SIZE_TEMPLATE = "The file %s exceeds its maximum permitted size of %s bytes.";

    @Autowired
    private JpaFileRepository jpaFileRepository;

    @Autowired
    private FileUploadProperties fileUploadProperties;

    private static StorageProperties storageProperties;

    @Autowired
    public void setStorageProperties(StorageProperties storageProperties) {
        FileRestControllerTest.storageProperties = storageProperties;
    }

    @AfterAll
    public static void after() throws IOException {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

        if (filesDirectoryPath.toFile().listFiles() != null ) {
            for (java.io.File file : Objects.requireNonNull(filesDirectoryPath.toFile().listFiles())) {
                Files.delete(file.toPath());
            }
        }
    }

    @Test
    void whenCreateFile_thenOk() throws Exception {
        MockMultipartFile mockMultipartFile = randomTxtFile();
        assertFile(ok(createFile(mockMultipartFile)), mockMultipartFile);
    }

    @Test
    void givenFileHasEmptyContentType_whenCreate_thenOk() throws Exception {
        MockMultipartFile multipartFile = randomFile(randomAlphabetic(10), 10, null);
        assertFile(ok(createFile(multipartFile)), multipartFile);
    }

    @Test
    void whenGetFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(createFile(randomTxtFile())));
        ok(get(fileId));
    }

    @Test
    void whenDeleteFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(createFile(randomTxtFile())));
        ok(delete(fileId));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenBadRequest() throws Exception {
        String invalidFileName = randomAlphabetic(10) + ".";

        MockMultipartFile multipartFile = randomTxtFile(invalidFileName);
        assertErrorMessage(badRequest(createFile(multipartFile)), INVALID_FILE_NAME_TEMPLATE,
                multipartFile.getOriginalFilename());
    }

    @Test
    void givenFileNowExistsInDatabase_whenGet_thenNotFound() throws Exception {
        Long notExistingFileId = getNotExistingFileId();
        assertErrorMessage(notFound(get(notExistingFileId)), RESOURCE_NOT_FOUND_TEMPLATE, notExistingFileId);
    }

    @Test
    void givenFileEmpty_whenCreateFile_thenBadRequest() throws Exception {
        assertErrorMessage(badRequest(mvc.perform(MockMvcRequestBuilders.multipart("/files"))),
                FILE_EMPTY_REQUEST_TEMPLATE);
    }

    @Test
    void givenFileNameLengthLargeThan20_whenCreate_thenBadRequest() throws Exception {
        String fileName = randomAlphabetic(fileUploadProperties.getMaxNameLength() + 2);
        MockMultipartFile multipartFile = randomTxtFile(fileName);
        assertErrorMessage(badRequest(createFile(multipartFile)), MAX_FILE_NAME_TEMPLATE,
                multipartFile.getOriginalFilename(), fileUploadProperties.getMaxNameLength());
    }

    @Test
    void givenFileSizeLargerThenMaxUploadSize_whenCreate_thenBadRequest() throws Exception {
        int fileSize = Integer.parseInt(String.valueOf(fileUploadProperties.getMaxSize() + 1));
        MockMultipartFile multipartFile = randomTxtFile(fileSize);
        assertErrorMessage(badRequest(createFile(multipartFile)),
                MAX_FILE_SIZE_TEMPLATE, multipartFile.getOriginalFilename(), fileUploadProperties.getMaxSize());
    }

    @Test
    void givenFileUploadedTwice_whenDeleteFirst_thenSecondExists() throws Exception {
        MockMultipartFile multipartFile = randomTxtFile();

        Long firstFileId = getFileId(ok(createFile(multipartFile)));
        Long secondFileId = getFileId(ok(createFile(multipartFile)));

        ok(delete(firstFileId));

        ok(get(secondFileId));
    }

    private ResultActions get(@NotNull Long id) throws Exception {
        return get("/files/" + id);
    }

    private ResultActions delete(@NotNull Long id) throws Exception {
        return delete("/files/" + id);
    }

    private void assertFile(ResultActions resultActions, MockMultipartFile mockMultipartFile) throws Exception {
        resultActions.andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(mockMultipartFile.getOriginalFilename())))
                .andExpect(jsonPath("$.uploadedOn", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(startsWith("/api/v1/files/"))));

        if (mockMultipartFile.getContentType() != null) {
            resultActions.andExpect(jsonPath("$.contentType", is(notNullValue())))
                    .andExpect(jsonPath("$.contentType", is(mockMultipartFile.getContentType())));
        }
    }

    private Long getNotExistingFileId() {
        long notExistingFileId = Long.parseLong(randomNumeric(3));

        while (jpaFileRepository.findById(notExistingFileId).isPresent()) {
            notExistingFileId = Long.parseLong(randomNumeric(3));
        }

        return notExistingFileId;
    }

}