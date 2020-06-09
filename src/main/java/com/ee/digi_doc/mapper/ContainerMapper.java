package com.ee.digi_doc.mapper;

import com.ee.digi_doc.mapper.processor.ContainerProcessor;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.dto.ContainerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContainerProcessor.class})
public interface ContainerMapper {

    @Mapping(target = "url", ignore = true)
    ContainerDto toDto(Container container);

}
