package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.digidoc4j.DataToSign;

import java.util.Collection;

public interface FileSigner {

    SigningData generateDataToSign(Collection<File> files, String certificateInHex);

    Container signContainer(SigningData signingData, String signatureInHex);

    @Getter
    @RequiredArgsConstructor
    class SigningData {
        private final org.digidoc4j.Container container;
        private final DataToSign dataToSign;
    }

    @Getter
    @RequiredArgsConstructor
    class Container {
        private final org.digidoc4j.Container bdDocContainer;
        private final String contentType;
    }

}
