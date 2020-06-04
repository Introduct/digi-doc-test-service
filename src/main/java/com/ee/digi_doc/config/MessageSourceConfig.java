package com.ee.digi_doc.config;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import javax.validation.MessageInterpolator;

@Configuration
public class MessageSourceConfig {

    private static final String MESSAGE_SOURCE_BASE_NAME = "classpath:messages/messages";
    private static final String MESSAGE_SOURCE_ENCODING = "UTF-8";

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(MESSAGE_SOURCE_BASE_NAME);
        messageSource.setDefaultEncoding(MESSAGE_SOURCE_ENCODING);
        return messageSource;
    }

    @Bean
    public MessageInterpolator messageInterpolator(MessageSource messageSource) {
        return new ResourceBundleMessageInterpolator(
                new MessageSourceResourceBundleLocator(messageSource), false);
    }

    @Bean
    public LocalValidatorFactoryBean validationFactoryBean(MessageInterpolator messageInterpolator) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setMessageInterpolator(messageInterpolator);
        return bean;
    }

}
