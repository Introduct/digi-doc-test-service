package com.ee.digi_doc.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private File file = new File();
    private Container container = new Container();
    private SigningData signingData = new SigningData();

    @Getter
    @Setter
    public static class File {
        private String path;
    }

    @Getter
    @Setter
    public static class Container {
        private String path;
    }

    @Getter
    @Setter
    public static class SigningData {
        private String path;
    }

}
