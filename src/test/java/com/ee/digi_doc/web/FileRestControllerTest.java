package com.ee.digi_doc.web;

import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.util.FileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class FileRestControllerTest {

    public static final String INVALID_FILE_NAME_TEMPLATE = "Invalid file name %s.";
    public static final String DATABASE_FILE_NOT_FOUND_TEMPLATE = "File with id %s has not been found in database.";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FileService fileService;

    @Test
    void whenCreateFile_thenOk() throws Exception {
        MockMultipartFile mockMultipartFile = FileGenerator.randomMultipartJpeg();
        assertFile(ok(create(mockMultipartFile)), mockMultipartFile);
    }

    @Test
    void whenGetFile_thenOk() throws Exception {
        File file = fileService.create(FileGenerator.randomMultipartJpeg());
        ok(get(file.getId()));
    }

    @Test
    void whenDeleteFile_thenOk() throws Exception {
        File file = fileService.create(FileGenerator.randomMultipartJpeg());
        ok(delete(file.getId()));
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
        assertErrorMessage(notFound(get(notExistingFileId)), DATABASE_FILE_NOT_FOUND_TEMPLATE, notExistingFileId);
    }

    private ResultActions ok(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isOk());
    }

    private ResultActions badRequest(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isBadRequest());
    }

    private ResultActions notFound(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isNotFound());
    }

    private ResultActions create(MockMultipartFile multipartFile) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.multipart("/files").file(multipartFile));
    }

    private ResultActions get(@NotNull Long id) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get("/files/" + id));
    }

    private ResultActions delete(@NotNull Long id) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete("/files/" + id));
    }

    private void assertFile(ResultActions resultActions, MockMultipartFile mockMultipartFile) throws Exception {
        resultActions.andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.fileName", is(notNullValue())))
                .andExpect(jsonPath("$.fileName", is(mockMultipartFile.getOriginalFilename())))
                .andExpect(jsonPath("$.contentType", is(notNullValue())))
                .andExpect(jsonPath("$.contentType", is(mockMultipartFile.getContentType())))
                .andExpect(jsonPath("$.uploadedOn", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(notNullValue())))
                .andExpect(jsonPath("$.url", is(startsWith("/api/v1/files/"))));
    }

    private void assertErrorMessage(ResultActions resultActions, String errorMessageTemplate, Object argument)
            throws Exception {
        resultActions.andExpect(jsonPath("$.message", is(String.format(errorMessageTemplate, argument))));
    }

}