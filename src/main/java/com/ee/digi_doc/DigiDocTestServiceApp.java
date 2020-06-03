package com.ee.digi_doc;

import com.ee.digi_doc.common.properties.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class DigiDocTestServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(DigiDocTestServiceApp.class, args);
    }

}
