package com.ee.digi_doc.mapper;

import com.ee.digi_doc.mapper.processor.FileProcessor;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.web.dto.FileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {FileProcessor.class})
public interface FileMapper {

    @Mapping(target = "url", ignore = true)
    FileDto toDto(File file);

}
