package com.ee.digi_doc.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.digidoc4j.Configuration;
import org.digidoc4j.DigestAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "digidoc4j")
public class Digidoc4jProperties {

    private DigestAlgorithm algorithm = DigestAlgorithm.SHA256;
    private Configuration.Mode mode = Configuration.Mode.PROD;

}
