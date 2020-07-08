package com.ee.digi_doc.web.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidFileIdsCount.Validator.class)
public @interface ValidFileIdsCount {

    String message() default "{com.ee.digi_doc.web.validator.ValidFileIdsCount.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidFileIdsCount, List<Long>> {

        @Value("${sign.max-file-count}")
        private int maxFileToSignCount;

        @Override
        public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
            return CollectionUtils.isEmpty(value) || value.size() <= maxFileToSignCount;
        }
    }

}
