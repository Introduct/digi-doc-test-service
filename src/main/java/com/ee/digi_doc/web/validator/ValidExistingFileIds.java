package com.ee.digi_doc.web.validator;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
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

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidExistingFileIds.Validator.class)
public @interface ValidExistingFileIds {

    String message() default "{com.ee.digi_doc.web.validator.ValidExistingFileIds.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @RequiredArgsConstructor
    class Validator implements ConstraintValidator<ValidExistingFileIds, List<Long>> {

        private final JpaFileRepository repository;

        @Override
        public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
            return CollectionUtils.isEmpty(value) || repository.findAllById(value).size() == value.size();
        }
    }

}
