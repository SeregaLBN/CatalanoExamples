package ksn.imgusage.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTest {

    @BeforeAll
    static void initLogger() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY  , "TRACE");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY     , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY   , "HH:mm:ss:SSS");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY   , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_LOG_NAME_KEY      , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
    }

    private static final Logger logger = LoggerFactory.getLogger(UnitTest.class);

    @Test
    public void relativePathTest() {
        File path = new File("/var/data/stuff/xyz.dat");
        File base = new File("/var/data");
        File relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals(u("stuff/xyz.dat"), relative.toString());


        path = new File("/proj/java/ImageFilterExamples/exampleImages/image.jpg");
        base = new File("/proj/java/ImageFilterExamples/exampleImages/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals("image.jpg", relative.toString());


        path = new File("./exampleImages/image.jpg");
        base = new File("./exampleImages/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals("image.jpg", relative.toString());


        path = new File("/path/dir1/image.jpg");
        base = new File("/path/dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());


        path = new File("path/dir1/image.jpg");
        base = new File("path/dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());


        path = new File("dir1/image.jpg");
        base = new File("dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.info("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());
    }

    private static String u(String path) {
        return path.replace("/", File.separator);
    }
    
}
