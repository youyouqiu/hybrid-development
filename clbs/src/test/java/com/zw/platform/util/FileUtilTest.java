package com.zw.platform.util;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class FileUtilTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    @Test
    public void save() throws IOException {
        String filePath = "target/test.txt";
        FileUtil.save(filePath, "test".getBytes());
        Path path = Paths.get(filePath);
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).contains("test"));
        Files.delete(path);
    }
}
