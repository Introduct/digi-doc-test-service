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
@Constraint(validatedBy = ValidFileIds.Validator.class)
public @interface ValidFileIds {

    String message() default "{com.ee.digi_doc.web.validator.ValidFileIds.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @RequiredArgsConstructor
    class Validator implements ConstraintValidator<ValidFileIds, List<Long>> {

        private final JpaFileRepository jpaFileRepository;

        @Override
        public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
            if (CollectionUtils.isEmpty(value)) {
                return true;
            }
            return jpaFileRepository.findAllById(value).size() == value.size();
        }

    }
}
