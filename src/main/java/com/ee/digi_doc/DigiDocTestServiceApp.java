package com.ee.digi_doc;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, Digidoc4jProperties.class})
public class DigiDocTestServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(DigiDocTestServiceApp.class, args);
    }

}
