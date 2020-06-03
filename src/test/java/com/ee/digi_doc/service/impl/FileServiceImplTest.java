package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.common.properties.FileStorageProperties;
import com.ee.digi_doc.exception.InvalidFileNameException;
import com.ee.digi_doc.exception.ResourceNotFoundException;
import com.ee.digi_doc.persistance.dao.FileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileService;
import com.ee.digi_doc.util.FileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class FileServiceImplTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    @Qualifier("fileServiceImpl")
    private FileService service;

    @Autowired
    private FileRepository repository;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Test
    void whenCreateFile_thenOk() {
        Path filesDirectoryPath = Paths.get(fileStorageProperties.getDirectory()).toAbsolutePath().normalize();

        MockMultipartFile expectedMultipartFile = FileGenerator.randomMultipartJpeg();
        File actualFile = service.create(expectedMultipartFile);

        assertNotNull(actualFile);
        assertNotNull(actualFile.getId());
        assertNotNull(actualFile.getFileName());
        assertNotNull(actualFile.getContentType());
        assertNotNull(actualFile.getUploadedOn());

        assertEquals(expectedMultipartFile.getOriginalFilename(), actualFile.getFileName());
        assertEquals(expectedMultipartFile.getContentType(), actualFile.getContentType());
        assertEquals(now().format(DATE_TIME_FORMATTER), actualFile.getUploadedOn().format(DATE_TIME_FORMATTER));

        assertTrue(repository.findById(actualFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(actualFile.getFileName())));
    }

    @Test
    void whenGetFile_thenOk() {
        Path filesDirectoryPath = Paths.get(fileStorageProperties.getDirectory()).toAbsolutePath().normalize();

        File expectedFile = service.create(FileGenerator.randomMultipartJpeg());

        assertTrue(repository.findById(expectedFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(expectedFile.getFileName())));

        File actualFile = service.get(expectedFile.getId());

        assertNotNull(actualFile);
        assertNotNull(actualFile.getId());
        assertNotNull(actualFile.getFileName());
        assertNotNull(actualFile.getContentType());
        assertNotNull(actualFile.getUploadedOn());
        assertNotNull(actualFile.getContent());

        assertEquals(expectedFile.getFileName(), actualFile.getFileName());
        assertEquals(expectedFile.getContentType(), actualFile.getContentType());
        assertEquals(expectedFile.getUploadedOn(), actualFile.getUploadedOn());
    }

    @Test
    void whenDeleteFile_thenOk() {
        Path filesDirectoryPath = Paths.get(fileStorageProperties.getDirectory()).toAbsolutePath().normalize();

        File createdFile = service.create(FileGenerator.randomMultipartJpeg());

        assertTrue(repository.findById(createdFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(createdFile.getFileName())));

        service.delete(createdFile.getId());

        assertTrue(repository.findById(createdFile.getId()).isEmpty());
        assertTrue(Files.notExists(filesDirectoryPath.resolve(createdFile.getFileName())));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenExceptionThrown() {
        assertThrows(InvalidFileNameException.class, () -> {
            String fileName = randomAlphabetic(10) + ".";
            service.create(FileGenerator.randomMultipartJpeg(fileName));
        });
    }

    @Test
    void givenFileNowExistsInDatabase_whenGet_thenExceptionThrown() {
        assertThrows(ResourceNotFoundException.class, () -> service.get(Long.valueOf(randomNumeric(5))));
    }

    @Test
    void givenFileNameLengthLargeThan20_whenCreate_thenExceptionThrown() {
        Path filesDirectoryPath = Paths.get(fileStorageProperties.getDirectory()).toAbsolutePath().normalize();

        String fileName = randomAlphabetic(21);
        MockMultipartFile multipartFile = FileGenerator.randomMultipartJpeg(fileName);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> service.create(multipartFile));

        assertNotNull(exception.getRootCause());
        assertTrue(exception.getRootCause().getMessage().contains("Value too long for column \"FILE_NAME VARCHAR(20)\""));

        assertNotNull(multipartFile.getOriginalFilename());
        assertTrue(Files.notExists(filesDirectoryPath.resolve(multipartFile.getOriginalFilename())));

    }

}