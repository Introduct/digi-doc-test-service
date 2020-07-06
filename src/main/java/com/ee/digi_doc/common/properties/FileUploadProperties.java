package com.ee.digi_doc.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "upload")
public class FileUploadProperties {

    private int maxNameLength;
    private long maxSize;

}
