package com.ee.digi_doc.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
public abstract class AbstractPurgeTask<T> {

    @Transactional
    @Scheduled(cron = "${cleanup.cron.expression}")
    public void cleanUp() {
        log.info("Start cleaning up");
        findEntitiesToCleanUp().forEach(this::cleanUpEntity);
        log.info("Cleaning up has been finished");
    }

    protected abstract Collection<T> findEntitiesToCleanUp();

    protected abstract void cleanUpEntity(T entity);

}
