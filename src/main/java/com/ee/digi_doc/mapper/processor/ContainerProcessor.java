package com.ee.digi_doc.mapper.processor;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.dto.ContainerDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public class ContainerProcessor {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @AfterMapping
    public void process(Container source, @MappingTarget ContainerDto target) {
        target.setUrl(contextPath + "/containers/" + source.getId());
    }

}
