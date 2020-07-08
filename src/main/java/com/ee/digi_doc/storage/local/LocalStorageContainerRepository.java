package com.ee.digi_doc.storage.local;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.common.properties.StorageProperties;
import com.ee.digi_doc.exception.FileNotDeletedException;
import com.ee.digi_doc.exception.FileNotReadException;
import com.ee.digi_doc.exception.FileNotWrittenException;
import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.storage.StorageContainerRepository;
import com.ee.digi_doc.storage.local.util.HexUtils;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Configuration;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Repository
public class LocalStorageContainerRepository implements StorageContainerRepository {

    private final Path containerStorageLocation;
    private final Configuration configuration;

    public LocalStorageContainerRepository(StorageProperties storageProperties, Digidoc4jProperties properties) {
        this.containerStorageLocation = Paths.get(storageProperties.getContainer().getPath()).toAbsolutePath().normalize();
        this.configuration = new Configuration(properties.getMode());
    }

    @Override
    public void storeContainer(Container container) {
        try {
            String containerHash = HexUtils.getContainerHex(container);
            log.debug("Container hash: {}", container);

            Path containerDirectoryPath = Files.createDirectories(containerStorageLocation.resolve(containerHash));
            log.debug("Container directory path: {}", containerDirectoryPath);

            Path containerPath = containerDirectoryPath.resolve(container.getName()).normalize();
            log.debug("Container path: {}", containerPath);

            try (OutputStream outputStream = new FileOutputStream(containerPath.toFile())) {
                container.getBdDocContainer().save(outputStream);
                log.info("BDOC container has been successfully stored");
            } catch (IOException e) {
                log.error("Error obtained during container write", e);
                throw new FileNotWrittenException(container.getId());
            }
        }catch (IOException e) {
            log.error("Error obtained during container write", e);
            throw new FileNotWrittenException(container.getId());
        }
    }

    @Override
    public org.digidoc4j.Container getContainer(Container container) {
        log.info("Get BDOC container: {}", container);

        Path containerPath = getContainerPath(container);
        log.debug("Container path: {}", containerPath);

        try (InputStream inputStream = new FileInputStream(containerPath.toFile())) {
            org.digidoc4j.Container bDocContainer = BDocContainerBuilder.aContainer()
                    .fromStream(inputStream)
                    .withConfiguration(configuration)
                    .build();
            log.info("BDOC container has been successfully found");
            return bDocContainer;
        } catch (IOException e) {
            log.error("Error obtained during container read", e);
            throw new FileNotReadException(container.getId());
        }
    }

    @Override
    public void deleteContainer(Container container) {
        try {
            log.info("Delete BDOC container: {}", container);

            Path containerPath = getContainerPath(container);
            log.debug("Container path: {}", containerPath);

            Files.delete(containerPath);
            log.info("BDOC container has been successfully deleted");
        } catch (IOException e) {
            log.error("Error obtained during container delete: ", e);
            throw new FileNotDeletedException(container.getId());
        }
    }

    private Path getContainerPath(Container container) {
        String containerHash = HexUtils.getContainerHex(container);
        log.debug("Container hash: {}", container);

        Path containerDirectoryPath = containerStorageLocation.resolve(containerHash);
        log.debug("Container directory path: {}", containerDirectoryPath);

        return containerDirectoryPath.resolve(container.getName()).normalize();
    }

}
