package com.ee.digi_doc.common.properties;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class FileStoragePropertiesTest {

    public static final String EXPECTED_FILE_STORAGE_LOCATION = "./var/files";

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Test
    void testFileStorageLocation() {
        assertNotNull(fileStorageProperties);
        assertEquals(EXPECTED_FILE_STORAGE_LOCATION, fileStorageProperties.getDirectory());
    }

}