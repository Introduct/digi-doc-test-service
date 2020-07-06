package com.ee.digi_doc.web.validator;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CreateSigningDataValidator implements Validator {

    @Value("${sign.max-file-count}")
    private int maxFileCount;

    private final JpaFileRepository jpaFileRepository;


    @Override
    public boolean supports(Class<?> aClass) {
        return CreateSigningDataRequest.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CreateSigningDataRequest request = (CreateSigningDataRequest) o;

        if (request.getCertificateInHex() == null || request.getCertificateInHex().isEmpty()) {
            errors.rejectValue("certificateInHex", "Validation.SigningData.EmptyCertificateInHex");
            return;
        }

        if (request.getFileIds() == null || request.getFileIds().isEmpty()) {
            errors.rejectValue("fileIds", "Validation.SigningData.EmptyFileIds");
            return;
        }

        if (request.getFileIds().size() > maxFileCount) {
            errors.rejectValue("fileIds", "Validation.SigningData.MaxFileCount", new Object[]{maxFileCount}, null);
            return;
        }

        if (jpaFileRepository.findAllById(request.getFileIds()).size() != request.getFileIds().size()) {
            errors.rejectValue("fileIds", "Validation.SigningData.NotAllFilesExist");
        }

    }
}
