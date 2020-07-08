package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.InvalidFileNameException;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.storage.local.util.HexUtils;
import com.ee.digi_doc.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ee.digi_doc.util.FileGenerator.randomFile;
import static com.ee.digi_doc.util.FileGenerator.randomTxtFile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class FileServiceTest {

    @Autowired
    private FileService service;

    @Autowired
    private JpaFileRepository repository;

    private static StorageProperties storageProperties;

    @Autowired
    public void setStorageProperties(StorageProperties storageProperties) {
        FileServiceTest.storageProperties = storageProperties;
    }

    @AfterAll
    public static void after() throws IOException {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath();
        FileUtils.cleanUp(filesDirectoryPath);
    }

    @Test
    void whenCreateFile_thenOk() {
        MockMultipartFile expectedMultipartFile = randomTxtFile();

        File actualFile = service.create(expectedMultipartFile);

        assertNotNull(actualFile);
        assertNotNull(actualFile.getId());
        assertNotNull(actualFile.getName());
        assertNotNull(actualFile.getContentType());
        assertNotNull(actualFile.getUploadedOn());

        assertEquals(expectedMultipartFile.getOriginalFilename(), actualFile.getName());
        assertEquals(expectedMultipartFile.getContentType(), actualFile.getContentType());

        assertTrue(repository.findById(actualFile.getId()).isPresent());
        assertTrue(Files.exists(getFilePath(actualFile)));
    }

    @Test
    void givenFileHasEmptyContentType_whenCreate_thenOk() {
        MockMultipartFile expectedMultipartFile = randomFile(randomAlphabetic(10), 10, null);

        File actualFile = service.create(expectedMultipartFile);

        assertNotNull(actualFile);
        assertNotNull(actualFile.getId());
        assertNotNull(actualFile.getName());
        assertNotNull(actualFile.getUploadedOn());
        assertNotNull(actualFile.getContentType());

        assertEquals(expectedMultipartFile.getOriginalFilename(), actualFile.getName());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, actualFile.getContentType());

        assertTrue(repository.findById(actualFile.getId()).isPresent());
        assertTrue(Files.exists(getFilePath(actualFile)));
    }

    @Test
    void whenGetFile_thenOk() {
        File expectedFile = service.create(randomTxtFile());

        Path expectedFilePath = getFilePath(expectedFile);

        assertTrue(repository.findById(expectedFile.getId()).isPresent());
        assertTrue(Files.exists(expectedFilePath));

        File actualFile = service.get(expectedFile.getId()).orElse(null);

        assertNotNull(actualFile);
        assertNotNull(actualFile.getId());
        assertNotNull(actualFile.getName());
        assertNotNull(actualFile.getContentType());
        assertNotNull(actualFile.getUploadedOn());
        assertNotNull(actualFile.getContent());

        assertEquals(expectedFile.getName(), actualFile.getName());
        assertEquals(expectedFile.getContentType(), actualFile.getContentType());
        assertEquals(expectedFile.getUploadedOn(), actualFile.getUploadedOn());
    }

    @Test
    void whenDeleteFileById_thenOk() {
        File createdFile = service.create(randomTxtFile());

        Path filePath = getFilePath(createdFile);

        assertTrue(repository.findById(createdFile.getId()).isPresent());
        assertTrue(Files.exists(filePath));

        service.delete(createdFile.getId());

        assertTrue(repository.findById(createdFile.getId()).isEmpty());
        assertTrue(Files.notExists(filePath));
    }

    @Test
    void whenDeleteFile_thenOk() {
        File createdFile = service.create(randomTxtFile());

        Path filePath = getFilePath(createdFile);

        assertTrue(repository.findById(createdFile.getId()).isPresent());
        assertTrue(Files.exists(filePath));

        service.delete(createdFile);

        assertTrue(repository.findById(createdFile.getId()).isEmpty());
        assertTrue(Files.notExists(filePath));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenExceptionThrown() {
        assertThrows(InvalidFileNameException.class, () -> {
            String fileName = randomAlphabetic(10) + ".";
            service.create(randomTxtFile(fileName));
        });
    }

    @Test
    void givenFileUploadedTwice_whenDeleteFirst_thenSecondExists() {
        MockMultipartFile multipartFile = randomTxtFile();

        File firstFile = service.create(multipartFile);
        File secondFile = service.create(multipartFile);

        Path firstFilePath = getFilePath(firstFile);
        Path secondFilePath = getFilePath(secondFile);

        assertTrue(repository.findById(firstFile.getId()).isPresent());
        assertTrue(Files.exists(firstFilePath));

        assertTrue(repository.findById(secondFile.getId()).isPresent());
        assertTrue(Files.exists(secondFilePath));

        service.delete(firstFile.getId());

        assertTrue(repository.findById(firstFile.getId()).isEmpty());
        assertTrue(Files.notExists(firstFilePath));

        assertTrue(service.get(secondFile.getId()).isPresent());

        assertTrue(repository.findById(secondFile.getId()).isPresent());
        assertTrue(Files.exists(secondFilePath));
    }

    private Path getFilePath(File file) {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath());
        String filePath = StringUtils.join(new Object[]{HexUtils.getFileHex(file), file.getName()}, "/");
        return filesDirectoryPath.resolve(filePath).normalize();
    }


}