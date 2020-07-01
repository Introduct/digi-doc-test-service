package com.ee.digi_doc.mapper;

import com.ee.digi_doc.mapper.processor.ContainerProcessor;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.dto.ContainerDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ContainerProcessor.class})
public interface ContainerMapper {

    ContainerDto toDto(Container container);

}
