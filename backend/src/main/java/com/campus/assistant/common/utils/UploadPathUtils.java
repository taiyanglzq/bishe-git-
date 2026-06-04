package com.campus.assistant.common.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathUtils {

    private UploadPathUtils() {
    }

    public static Path uploadDir() {
        Path workDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path projectRoot = workDir.getFileName() != null && "backend".equalsIgnoreCase(workDir.getFileName().toString())
                ? workDir.getParent()
                : workDir;
        return projectRoot.resolve("uploads").normalize();
    }
}
