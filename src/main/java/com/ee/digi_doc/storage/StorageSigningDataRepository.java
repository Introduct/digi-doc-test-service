package com.ee.digi_doc.storage;

import com.ee.digi_doc.persistance.model.SigningData;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;

public interface StorageSigningDataRepository {

    void storeSigningData(SigningData signingData);

    Container getContainer(String containerName);

    DataToSign getDataToSign(String dataToSignName);

    void deleteContainer(String containerName);

    void deleteDataToSigh(String dataToSignName);

}
