package com.ee.digi_doc;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.FileUploadProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, Digidoc4jProperties.class, FileUploadProperties.class})
public class DigiDocTestServiceApp {

    @Generated
    public static void main(String[] args) {
        SpringApplication.run(DigiDocTestServiceApp.class, args);
    }

}
