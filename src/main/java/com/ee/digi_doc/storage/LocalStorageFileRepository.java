package com.ee.digi_doc.storage;

import com.ee.digi_doc.persistance.model.File;

import java.util.Optional;

public interface LocalStorageFileRepository {

    void storeFile(File file);

    Optional<byte[]> getFileContent(String fileName);

    void deleteFile(String fileName);

}
