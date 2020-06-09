package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.File;
import eu.europa.esig.dss.spi.DSSUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;

import javax.xml.bind.DatatypeConverter;
import java.util.Collection;

public interface FileSigner {

    SigningData generateDataToSign(Collection<File> files, String certificateInHex);

    @RequiredArgsConstructor
    class SigningData {
        @Getter
        private final Container container;
        @Getter
        private final DataToSign dataToSign;

        public String getSignatureInHex() {
            return DatatypeConverter.printHexBinary(DSSUtils.digest(eu.europa.esig.dss.enumerations.DigestAlgorithm.SHA256,
                    dataToSign.getDataToSign()));
        }
    }

}
