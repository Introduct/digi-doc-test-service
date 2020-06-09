package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFileRepository extends JpaRepository<File, Long> {
}
