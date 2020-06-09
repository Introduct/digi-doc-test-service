package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.Container;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<Container, Long> {
}
