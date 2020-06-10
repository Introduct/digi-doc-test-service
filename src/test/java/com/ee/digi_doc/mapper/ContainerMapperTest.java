package com.ee.digi_doc.mapper;

import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.dto.ContainerDto;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class ContainerMapperTest {

    @Autowired
    private ContainerMapper mapper;

    @Autowired
    private JpaContainerRepository jpaContainerRepository;

    @Test
    void toDtoTest() {
        Container source = new Container();
        source.setName(RandomStringUtils.randomAlphabetic(10));
        source.setContentType(RandomStringUtils.randomAlphabetic(40));

        source = jpaContainerRepository.save(source);

        ContainerDto target = mapper.toDto(source);

        assertNotNull(target);
        assertNotNull(target.getId());
        assertNotNull(target.getName());
        assertNotNull(target.getSignedOn());
        assertNotNull(target.getUrl());

        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getSignedOn(), target.getSignedOn());
        assertEquals("/api/v1/containers/" + source.getId(), target.getUrl());

    }

}