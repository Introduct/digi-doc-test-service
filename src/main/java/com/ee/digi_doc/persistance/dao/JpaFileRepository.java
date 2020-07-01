package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaFileRepository extends JpaRepository<File, Long> {

    List<File> findAllByUploadedOnBefore(LocalDateTime uploadedOn);

}
