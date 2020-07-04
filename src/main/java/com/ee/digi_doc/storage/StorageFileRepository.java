package com.ee.digi_doc.storage;

import com.ee.digi_doc.persistance.model.File;

import java.util.Optional;

public interface StorageFileRepository {

    void storeFile(File file);

    Optional<byte[]> getFileContent(File file);

    void deleteFile(File file);

}
