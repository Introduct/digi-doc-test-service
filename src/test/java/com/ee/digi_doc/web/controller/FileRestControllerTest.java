package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.common.properties.FileUploadProperties;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.util.FileGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.validation.constraints.NotNull;

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

    @Test
    void whenCreateFile_thenOk() throws Exception {
        MockMultipartFile mockMultipartFile = FileGenerator.randomFile();
        assertFile(ok(createFile(mockMultipartFile)), mockMultipartFile);
    }

    @Test
    void whenGetFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(createFile(FileGenerator.randomFile())));
        ok(get(fileId));
    }

    @Test
    void whenDeleteFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(createFile(FileGenerator.randomFile())));
        ok(delete(fileId));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenBadRequest() throws Exception {
        String invalidFileName = randomAlphabetic(10) + ".";

        MockMultipartFile multipartFile = FileGenerator.randomFile(invalidFileName);
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
        MockMultipartFile multipartFile = FileGenerator.randomFile(fileName);
        assertErrorMessage(badRequest(createFile(multipartFile)), MAX_FILE_NAME_TEMPLATE,
                multipartFile.getOriginalFilename(), fileUploadProperties.getMaxNameLength());
    }

    @Test
    void givenFileSizeLargerThenMaxUploadSize_whenCreate_thenBadRequest() throws Exception {
        int fileSize = Integer.parseInt(String.valueOf(fileUploadProperties.getMaxSize() + 1));
        MockMultipartFile multipartFile = FileGenerator.randomFile(fileSize);
        assertErrorMessage(badRequest(createFile(multipartFile)),
                MAX_FILE_SIZE_TEMPLATE, multipartFile.getOriginalFilename(), fileUploadProperties.getMaxSize());
    }

    @Test
    void givenFileUploadedTwice_whenDeleteFirst_thenSecondExists() throws Exception {
        MockMultipartFile multipartFile = FileGenerator.randomFile();

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
                .andExpect(jsonPath("$.contentType", is(notNullValue())))
                .andExpect(jsonPath("$.contentType", is(mockMultipartFile.getContentType())))
                .andExpect(jsonPath("$.uploadedOn", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(startsWith("/api/v1/files/"))));
    }

    private Long getNotExistingFileId() {
        long notExistingFileId = Long.parseLong(randomNumeric(3));

        while (jpaFileRepository.findById(notExistingFileId).isPresent()) {
            notExistingFileId = Long.parseLong(randomNumeric(3));
        }

        return notExistingFileId;
    }

}