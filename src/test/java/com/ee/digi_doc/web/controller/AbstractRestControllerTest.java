package com.ee.digi_doc.web.controller;

import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.util.TestSigningData;
import com.ee.digi_doc.web.dto.FileDto;
import com.ee.digi_doc.web.dto.SigningDataDto;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public abstract class AbstractRestControllerTest {

    protected static final String RESOURCE_NOT_FOUND_TEMPLATE = "Resource with id %s has not been found.";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected final ResultActions ok(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isOk());
    }

    protected final ResultActions badRequest(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isBadRequest());
    }

    protected final ResultActions notFound(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isNotFound());
    }

    protected final ResultActions multiPart(@NotEmpty String url, MockMultipartFile multipartFile) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.multipart(url).file(multipartFile));
    }

    protected final ResultActions get(@NotEmpty String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(url));
    }

    protected final ResultActions delete(@NotEmpty String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(url));
    }

    protected final ResultActions postJson(@NotEmpty String url, @NotNull Object request) throws Exception {
        return mvc.perform(json(MockMvcRequestBuilders.post(url), request));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, Object request) throws Exception {
        return builder.content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON);
    }

    protected final void assertErrorMessage(ResultActions resultActions, String errorMessageTemplate, Object argument)
            throws Exception {
        resultActions.andExpect(jsonPath("$.errorMessage", is(String.format(errorMessageTemplate, argument))));
    }

    protected final void assertFieldError(ResultActions resultActions, String error, String field, String message)
            throws Exception {
        resultActions
                .andExpect(jsonPath("errors[0].error", is(error)))
                .andExpect(jsonPath("errors[0].message", is(message)))
                .andExpect(jsonPath("errors[0].field", is(field)));
    }

    protected final Long getFileId(ResultActions resultActions) throws Exception {
        byte[] content = resultActions.andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readValue(content, FileDto.class).getId();
    }

    protected final ResultActions createFile(MockMultipartFile multipartFile) throws Exception {
        return multiPart("/files", multipartFile);
    }

    protected final ResultActions createSigningData(CreateSigningDataRequest request) throws Exception {
        return postJson("/signing-data", request);
    }

    protected final CreateSigningDataRequest createSigningDataRequest() throws Exception {
        List<Long> fileIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            fileIds.add(getFileId(ok(createFile(FileGenerator.randomMultipartJpeg()))));
        }

        CreateSigningDataRequest request = new CreateSigningDataRequest();
        request.setFileIds(fileIds);
        request.setCertificateInHex(TestSigningData.getRSASigningCertificateInHex());
        return request;
    }

    protected final SigningDataDto retrieveSigningDataDto(ResultActions resultActions) throws Exception {
        byte[] content = resultActions.andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readValue(content, SigningDataDto.class);
    }

}
