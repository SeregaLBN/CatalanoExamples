package ksn.imgusage.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class PassportUkrTest {

    private static final Logger logger = LoggerFabric.getLogger(PassportUkrTest.class);

    private void randomRotate(File imgFileIn, File imgFileOut) throws IOException {
        logger.debug("imgFileIn.exist={}; imgFileOut.exist={}", imgFileIn.exists(), imgFileOut.exists());
        logger.debug("imgFileIn.path={}", imgFileIn.getAbsolutePath());
        if (!imgFileIn.exists())
            throw new IllegalArgumentException("Input file not found " + imgFileIn.getAbsolutePath());


        BufferedImage imgIn = ImageIO.read(imgFileIn);

        Random rnd = ThreadLocalRandom.current();
        double angleMin = -50;
        double angleMax = +50;
        double angle = angleMin + rnd.nextInt((int)(angleMax - angleMin));
        BufferedImage imgOut = ImgHelper.rotate(imgIn, angle, false);
        imgOut = addBorder(imgOut, Color.WHITE);

        String ext = SelectFilterDialog.getExtension(imgFileOut);
        boolean succ = ImageIO.write(imgOut, ext, imgFileOut);
        if (succ)
            logger.info("Image saved to {} file {}\n Rotate angle os {}", ext, imgFileOut, angle);
        else
            logger.error("Can`t save image to {} file {}", ext, imgFileOut);
        Assertions.assertTrue(succ);
    }

    private BufferedImage addBorder(BufferedImage img, Color fillColor) {
        int w = img.getWidth();
        int h = img.getHeight();
        double dx = w / 10.0;
        double dy = h / 10.0;
        BufferedImage outImg = new BufferedImage(
                w + (int)dx,
                h + (int)dy,
                img.getType());

        Graphics2D g = outImg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(fillColor);
        g.fillRect(0, 0, w + (int)dx, h + (int)dy);
        g.drawImage(img,
                (int)dx/2, (int)dy/2, w + (int)dx/2, h + (int)dy/2,
                0, 0, w, h,
                fillColor, null);
        g.dispose();

        return outImg;
    }

    @Test
    public void randomRotateTest() throws IOException {
        randomRotate(
                Paths.get("exampleImages", "passportUkr.jpg"    ).toFile(),
                Paths.get("exampleImages", "passportUkr_rnd.png").toFile());
    }

    public static void main(String[] args) {
        try {
            new PassportUkrTest().randomRotateTest();
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

    }

}
