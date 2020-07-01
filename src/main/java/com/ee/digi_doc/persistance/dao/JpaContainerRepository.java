package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.Container;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaContainerRepository extends JpaRepository<Container, Long> {

    List<Container> findAllBySignedOnBefore(LocalDateTime signedOn);

}
