package com.ee.digi_doc.job;

import com.ee.digi_doc.persistance.dao.JpaContainerRepository;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.service.ContainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerPurgeTask extends AbstractPurgeTask<Container> {

    private final JpaContainerRepository jpaContainerRepository;
    private final ContainerService containerService;

    @Override
    protected Collection<Container> findEntitiesToCleanUp() {
        log.info("Find containers to clean up");
        List<Container> containers = jpaContainerRepository.findAllBySignedOnBefore(LocalDateTime.now().minusDays(2));
        log.debug("Containers to be cleaned up: {}", containers);
        return containers;
    }

    @Override
    protected void cleanUpEntity(Container entity) {
        log.info("Clean up container: {}", entity);
        containerService.delete(entity);
        log.debug("Container has been cleaned up");
    }
}
