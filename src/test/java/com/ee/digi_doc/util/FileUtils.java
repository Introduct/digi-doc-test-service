package com.ee.digi_doc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileUtils {

    public static void cleanUp(Path path) throws IOException {
        if (path.toFile().isDirectory()) {
            for (java.io.File file : Objects.requireNonNull(path.toFile().listFiles())) {
                cleanUp(file.toPath());
            }
        }
        Files.delete(path);
    }
}
