package com.ee.digi_doc.mapper.processor;

import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.web.dto.SigningDataDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public class SigningDataProcessor {

    @AfterMapping
    public void process(SigningData source, @MappingTarget SigningDataDto target) {
        target.setSignatureInHex(source.getSignatureInHex());
    }

}
