package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.SigningData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaSigningDataRepository extends JpaRepository<SigningData, Long> {

    List<SigningData> findAllByCreatedOnBefore(LocalDateTime createdOn);

}
