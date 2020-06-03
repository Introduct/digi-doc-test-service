package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
