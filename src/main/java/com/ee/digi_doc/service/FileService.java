package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.File;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public interface FileService {

    File create(MultipartFile multipartFile);

    File get(@NotNull Long id);

    void delete(@NotNull Long id);

}
