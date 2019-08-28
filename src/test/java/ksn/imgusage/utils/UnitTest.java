package ksn.imgusage.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class UnitTest {

    private static final Logger logger = LoggerFabric.getLogger(UnitTest.class);

    @Test
    public void relativePathTest() {
        File path = new File("/var/data/stuff/xyz.dat");
        File base = new File("/var/data");
        File relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals(u("stuff/xyz.dat"), relative.toString());


        path = new File("/proj/java/ImageFilterExamples/exampleImages/image.jpg");
        base = new File("/proj/java/ImageFilterExamples/exampleImages/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals("image.jpg", relative.toString());


        path = new File("./exampleImages/image.jpg");
        base = new File("./exampleImages/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals("image.jpg", relative.toString());


        path = new File("/path/dir1/image.jpg");
        base = new File("/path/dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());


        path = new File("path/dir1/image.jpg");
        base = new File("path/dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());


        path = new File("dir1/image.jpg");
        base = new File("dir2/image.pipeline.json").getParentFile();
        relative = SelectFilterDialog.getRelativePath(path, base);
        logger.debug("relative={}", relative);
        assertEquals(u("../dir1/image.jpg"), relative.toString());
    }

    private static String u(String path) {
        return path.replace("/", File.separator);
    }

}
