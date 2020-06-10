package com.ee.digi_doc.persistance.dao;

import com.ee.digi_doc.persistance.model.SigningData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSigningDataRepository extends JpaRepository<SigningData, Long> {
}
