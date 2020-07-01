package com.ee.digi_doc.mapper;

import com.ee.digi_doc.mapper.processor.SigningDataProcessor;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.web.dto.SigningDataDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SigningDataProcessor.class})
public interface SigningDataMapper {

    SigningDataDto toDto(SigningData signingData);

}
