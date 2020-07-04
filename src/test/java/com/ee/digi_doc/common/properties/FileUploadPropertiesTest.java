package com.ee.digi_doc.common.properties;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
class FileUploadPropertiesTest {

    @Value("${upload.max-name-length}")
    private int maxNameLength;
    @Value("${upload.max-size}")
    private long maxSize;

    @Autowired
    private FileUploadProperties properties;

    @Test
    void testUploadProperties() {
        assertNotNull(properties);
        assertEquals(maxNameLength, properties.getMaxNameLength());
        assertEquals(maxSize, properties.getMaxSize());
    }

}