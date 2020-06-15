package com.ee.digi_doc.mapper;

import com.ee.digi_doc.persistance.dao.JpaFileRepository;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.util.FileGenerator;
import com.ee.digi_doc.web.dto.FileDto;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class FileMapperTest {

    @Autowired
    private FileMapper mapper;

    @Autowired
    private JpaFileRepository jpaFileRepository;

    @Test
    void toDtoTest() {
        File source = jpaFileRepository.save(File.of(FileGenerator.randomMultipartJpeg()));
        FileDto target = mapper.toDto(source);

        assertNotNull(target);
        assertNotNull(target.getId());
        assertNotNull(target.getName());
        assertNotNull(target.getContentType());
        assertNotNull(target.getUploadedOn());
        assertNotNull(target.getUrl());

        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getContentType(), target.getContentType());
        assertEquals(source.getUploadedOn(), target.getUploadedOn());
        assertEquals("/api/v1/files/" + source.getId(), target.getUrl());
    }

    @Test
    void givenInputNull_whenToDto_thenOutputNull() {
        assertNull(mapper.toDto(null));
    }

}