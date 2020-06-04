package com.ee.digi_doc.mapper.processor;

import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.web.dto.FileDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public class FileProcessor {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @AfterMapping
    public void process(File file, @MappingTarget FileDto fileDto) {
        fileDto.setUrl(contextPath + "/files/" + file.getId());
    }

}
