package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.dao.JpaSigningDataRepository;
import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.service.SigningDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SigningDataPurgeTask extends AbstractPurgeTask<SigningData> {

    private final JpaSigningDataRepository jpaSigningDataRepository;
    private final SigningDataService signingDataService;


    @Override
    protected Collection<SigningData> findEntitiesToCleanUp() {
        log.info("Find signing data to clean up");
        List<SigningData> signingDataList = jpaSigningDataRepository.findAllByCreatedOnBefore(LocalDateTime.now().minusDays(2));
        log.debug("Signing data to be cleaned up: {}", signingDataList);
        return signingDataList;
    }

    @Override
    protected void cleanUpEntity(SigningData entity) {
        log.info("Clean up signing data: {}", entity);
        signingDataService.delete(entity);
        log.debug("Signing data has been cleaned up");
    }
}
