package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;

import java.util.Collection;

public interface FileSigner {

    SigningData generateDataToSign(Collection<File> files, String certificateInHex);

    Container signContainer(SigningData signingData, String signatureInHex);

}
