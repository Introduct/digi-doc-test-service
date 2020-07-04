package com.ee.digi_doc.web.validator;


import com.ee.digi_doc.common.properties.FileUploadProperties;
import com.ee.digi_doc.web.request.UploadFileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class FileUploadValidator implements Validator {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public boolean supports(Class<?> clazz) {
        return UploadFileRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        UploadFileRequest request = (UploadFileRequest) object;

        if (request.getFile() == null) {
            errors.reject("Validation.File.EmptyRequest");
            return;
        }

        if (request.getFile().getOriginalFilename().length() > fileUploadProperties.getMaxNameLength()) {
            Object[] arguments = {request.getFile().getOriginalFilename(), fileUploadProperties.getMaxNameLength()};
            errors.reject("Validation.File.MaxFileNameExceeded", arguments, null);
            return;
        }

        if (request.getFile().getSize() > fileUploadProperties.getMaxSize()) {
            Object[] arguments = {request.getFile().getOriginalFilename(), fileUploadProperties.getMaxSize()};
            errors.reject("Validation.File.MaxUploadSizeExceeded", arguments, null);
        }
    }
}
