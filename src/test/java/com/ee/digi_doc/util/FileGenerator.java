package com.ee.digi_doc.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class FileGenerator {

    public static MockMultipartFile randomTxtFile() {
        return randomTxtFile(randomAlphanumeric(10), 10);
    }

    public static MockMultipartFile randomTxtFile(String fileName) {
        return randomTxtFile(fileName, 10);
    }

    public static MockMultipartFile randomTxtFile(int size) {
        return randomTxtFile(randomAlphanumeric(10), size);
    }

    public static MockMultipartFile randomTxtFile(String filename, int size) {
        return randomFile(StringUtils.join(new String[]{filename, "txt"}, "."), size, TEXT_PLAIN_VALUE);
    }

    public static MockMultipartFile randomFile(String fileName, int size, String contentType) {
        return new MockMultipartFile("file", fileName, contentType, randomAlphanumeric(size).getBytes());
    }

}
