package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.util.FileGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FileRestControllerTest extends AbstractRestControllerTest {

    private static final String INVALID_FILE_NAME_TEMPLATE = "Invalid file name %s.";

    @Test
    void whenCreateFile_thenOk() throws Exception {
        MockMultipartFile mockMultipartFile = FileGenerator.randomMultipartJpeg();
        assertFile(ok(create(mockMultipartFile)), mockMultipartFile);
    }

    @Test
    void whenGetFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(create(FileGenerator.randomMultipartJpeg())));
        ok(get(fileId));
    }

    @Test
    void whenDeleteFile_thenOk() throws Exception {
        Long fileId = getFileId(ok(create(FileGenerator.randomMultipartJpeg())));
        ok(delete(fileId));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenBadRequest() throws Exception {
        String invalidFileName = randomAlphabetic(10) + ".";

        MockMultipartFile multipartFile = FileGenerator.randomMultipartJpeg(invalidFileName);
        assertErrorMessage(badRequest(create(multipartFile)), INVALID_FILE_NAME_TEMPLATE,
                multipartFile.getOriginalFilename());
    }

    @Test
    void givenFileNowExistsInDatabase_whenGet_thenNotFound() throws Exception {
        Long notExistingFileId = Long.valueOf(randomNumeric(3));
        assertErrorMessage(notFound(get(notExistingFileId)), RESOURCE_NOT_FOUND_TEMPLATE, notExistingFileId);
    }

    private ResultActions create(MockMultipartFile multipartFile) throws Exception {
        return multiPart("/files", multipartFile);
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

}