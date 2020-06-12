package com.ee.digi_doc.storage;

import com.ee.digi_doc.persistance.model.Container;

public interface StorageContainerRepository {

    void storeContainer(Container container);

    org.digidoc4j.Container getContainer(String containerName);

    void deleteContainer(String containerName);

}
