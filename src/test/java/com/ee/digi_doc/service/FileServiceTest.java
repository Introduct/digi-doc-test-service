package com.ee.digi_doc.service;

import com.ee.digi_doc.common.properties.FileUploadProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.InvalidFileNameException;
import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ee.digi_doc.storage.local.LocalStorageFileRepository.getUniqueFileName;
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

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private FileUploadProperties fileUploadProperties;

    @Test
    void whenCreateFile_thenOk() {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

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
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(actualFile))));
    }

    @Test
    void givenFileHasEmptyContentType_whenCreate_thenOk() {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

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
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(actualFile))));
    }

    @Test
    void whenGetFile_thenOk() {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

        File expectedFile = service.create(randomTxtFile());

        assertTrue(repository.findById(expectedFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(expectedFile))));

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
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

        File createdFile = service.create(randomTxtFile());

        assertTrue(repository.findById(createdFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(createdFile))));

        service.delete(createdFile.getId());

        assertTrue(repository.findById(createdFile.getId()).isEmpty());
        assertTrue(Files.notExists(filesDirectoryPath.resolve(getUniqueFileName(createdFile))));
    }

    @Test
    void whenDeleteFile_thenOk() {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

        File createdFile = service.create(randomTxtFile());

        assertTrue(repository.findById(createdFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(createdFile))));

        service.delete(createdFile);

        assertTrue(repository.findById(createdFile.getId()).isEmpty());
        assertTrue(Files.notExists(filesDirectoryPath.resolve(getUniqueFileName(createdFile))));
    }

    @Test
    void givenInvalidFileName_whenCreate_thenExceptionThrown() {
        assertThrows(InvalidFileNameException.class, () -> {
            String fileName = randomAlphabetic(10) + ".";
            service.create(randomTxtFile(fileName));
        });
    }

    @Test
    void givenFileNameLengthLargeThan20_whenCreate_thenExceptionThrown() {
        String fileName = randomAlphabetic(fileUploadProperties.getMaxNameLength() + 1);
        MockMultipartFile multipartFile = randomTxtFile(fileName);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> service.create(multipartFile));

        assertNotNull(exception.getRootCause());
        assertTrue(exception.getRootCause().getMessage().contains("Value too long for column"));
    }

    @Test
    void givenFileUploadedTwice_whenDeleteFirst_thenSecondExists() {
        Path filesDirectoryPath = Paths.get(storageProperties.getFile().getPath()).toAbsolutePath().normalize();

        MockMultipartFile multipartFile = randomTxtFile();
        File firstFile = service.create(multipartFile);
        File secondFile = service.create(multipartFile);

        assertTrue(repository.findById(firstFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(firstFile))));

        assertTrue(repository.findById(secondFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(secondFile))));

        service.delete(firstFile.getId());

        assertTrue(repository.findById(firstFile.getId()).isEmpty());
        assertTrue(Files.notExists(filesDirectoryPath.resolve(getUniqueFileName(firstFile))));

        assertTrue(service.get(secondFile.getId()).isPresent());

        assertTrue(repository.findById(secondFile.getId()).isPresent());
        assertTrue(Files.exists(filesDirectoryPath.resolve(getUniqueFileName(secondFile))));
    }

}