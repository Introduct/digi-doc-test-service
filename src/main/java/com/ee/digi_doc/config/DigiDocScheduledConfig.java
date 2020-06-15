package com.ee.digi_doc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan("com.ee.digi_doc.job")
public class DigiDocScheduledConfig {

}
