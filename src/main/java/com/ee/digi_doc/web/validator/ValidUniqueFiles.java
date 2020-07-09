package com.ee.digi_doc.web.validator;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

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
@Constraint(validatedBy = ValidUniqueFiles.Validator.class)
public @interface ValidUniqueFiles {

    String message() default "{com.ee.digi_doc.web.validator.ValidUniqueFiles.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @RequiredArgsConstructor
    class Validator implements ConstraintValidator<ValidUniqueFiles, List<Long>> {

        private final JpaFileRepository jpaFileRepository;

        @Override
        public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
            return CollectionUtils.isEmpty(value) || jpaFileRepository.findAllById(value)
                    .stream()
                    .map(File::getName)
                    .collect(Collectors.toSet()).size() == value.size();
        }
    }

}
