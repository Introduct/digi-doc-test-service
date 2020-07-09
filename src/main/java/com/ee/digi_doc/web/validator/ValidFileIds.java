package com.ee.digi_doc.web.validator;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ValidFileIds.Validator.class)
public @interface ValidFileIds {

    String message() default "{com.ee.digi_doc.web.validator.ValidFileIds.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @RequiredArgsConstructor
    class Validator implements ConstraintValidator<ValidFileIds, List<Long>> {

        static final String MAX_FILE_COUNT = "{com.ee.digi_doc.web.validator.ValidFileIds.MaxFileCount.message}";
        static final String FILES_NOT_FOUND = "{com.ee.digi_doc.web.validator.ValidFileIds.FileNotFound.message}";
        static final String FILES_DUPLICATES = "{com.ee.digi_doc.web.validator.ValidFileIds.FilesDuplicates.message}";

        @Value("${sign.max-file-count}")
        private int maxFileToSignCount;

        private final JpaFileRepository repository;

        @Override
        public boolean isValid(List<Long> fileIds, ConstraintValidatorContext context) {
            if (CollectionUtils.isEmpty(fileIds)) {
                return true;
            }

            context.disableDefaultConstraintViolation();

            if (fileIds.size() > maxFileToSignCount) {
                buildConstraint(context, MAX_FILE_COUNT);
                return false;
            }

            if (repository.findAllById(fileIds).size() < fileIds.size()) {
                buildConstraint(context, FILES_NOT_FOUND);
                return false;
            }

            if (existsFileDuplicates(fileIds)) {
                buildConstraint(context, FILES_DUPLICATES);
                return false;
            }

            return true;
        }

        private boolean existsFileDuplicates(List<Long> fileIds) {
            return repository.findAllById(fileIds)
                    .stream()
                    .map(File::getName)
                    .collect(Collectors.toSet()).size() < fileIds.size();
        }

        private void buildConstraint(ConstraintValidatorContext context, String messageTemplate) {
            context.buildConstraintViolationWithTemplate(messageTemplate)
                    .addConstraintViolation();
        }
    }

}
