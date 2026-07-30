package com.employeedirectory.tests;

import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertTrue;

public class BaseTestPathResolutionTest {

    @Test
    public void resolvesAppDirFromConfiguredProperty() throws Exception {
        Path tempDir = Files.createTempDirectory("app-dir-test");
        Path index = tempDir.resolve("index.html");
        Files.writeString(index, "<html></html>");

        System.setProperty("app.dir", tempDir.toString());

        try {
            Path resolved = invokeResolveAppDir();
            assertTrue(Files.isSameFile(resolved, tempDir.toAbsolutePath().normalize()));
        } finally {
            System.clearProperty("app.dir");
            deleteRecursively(tempDir);
        }
    }

    @Test
    public void resolvesAppDirFromParentDirectoryWhenIndexIsAtRepoRoot() throws Exception {
        Path tempRoot = Files.createTempDirectory("repo-root-test");
        Path moduleDir = tempRoot.resolve("docs").resolve("selenium-tests");
        Files.createDirectories(moduleDir);
        Path index = tempRoot.resolve("index.html");
        Files.writeString(index, "<html></html>");

        System.clearProperty("app.dir");
        System.setProperty("user.dir", moduleDir.toString());

        try {
            Path resolved = invokeResolveAppDir();
            assertTrue(Files.isSameFile(resolved, tempRoot.toAbsolutePath().normalize()));
        } finally {
            System.clearProperty("app.dir");
            System.clearProperty("user.dir");
            deleteRecursively(tempRoot);
        }
    }

    private static Path invokeResolveAppDir() throws Exception {
        Method method = BaseTest.class.getDeclaredMethod("resolveAppDir");
        method.setAccessible(true);
        return (Path) method.invoke(null);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (java.util.stream.Stream<Path> stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteRecursively(child);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw e;
            }
        }
        Files.deleteIfExists(path);
    }
}
