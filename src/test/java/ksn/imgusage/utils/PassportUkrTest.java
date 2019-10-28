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
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;

import ksn.imgusage.tabs.opencv.InitLib;
import ksn.imgusage.type.opencv.CvBorderTypes;

/** Prepare test images for demo pipelines:
 * <li> @see {@link #randomRotateTest()}      - used for pipeline demo: ./exampleImages/idCard.LeadToHorizont.json
 * <li> @see {@link #randomPerspectiveTest()} - used for pipeline demo: ./exampleImages/idCard.LeadToPerspective.json
 */
public class PassportUkrTest {

    private static final Logger logger = LoggerFabric.getLogger(PassportUkrTest.class);

    static {
        InitLib.loadOpenCV();
    }

    private static BufferedImage randomRotate(BufferedImage imgIn) {
        Random rnd = ThreadLocalRandom.current();
        double angleMin = -50;
        double angleMax = +50;
        double angle = angleMin + rnd.nextInt((int)(angleMax - angleMin));
        BufferedImage imgOut = ImgHelper.rotate(imgIn, angle, false);
        logger.info("Image rotate angle is {}", angle);
        return addBorder(imgOut, Color.WHITE, new Size(imgIn.getWidth() / 10.0,
                                                       imgIn.getHeight() / 10.0));
    }

    private static void randomRotate(File imgFileIn, File imgFileOut) throws IOException {
        logger.debug("imgFileIn.exist={}; imgFileOut.exist={}", imgFileIn.exists(), imgFileOut.exists());
        logger.debug("imgFileIn.path={}", imgFileIn.getAbsolutePath());
        if (!imgFileIn.exists())
            throw new IllegalArgumentException("Input file not found " + imgFileIn.getAbsolutePath());

        BufferedImage imgIn = ImageIO.read(imgFileIn);
        BufferedImage imgOut = randomRotate(imgIn);

        save(imgOut, imgFileOut);
    }

    private static BufferedImage randomPerspective(BufferedImage imgIn) {
        Random rnd = ThreadLocalRandom.current();

        int w = imgIn.getWidth();
        int h = imgIn.getHeight();
        double staticOffsetX  = w / 7.0;
        double staticOffsetY  = h / 7.0;
        double dynamicOffsetX = w / 20.0;
        double dynamicOffsetY = h / 20.0;
        double dx = staticOffsetX + dynamicOffsetX;
        double dy = staticOffsetY + dynamicOffsetY;
        Point offset1 = new Point(staticOffsetX + rnd.nextInt((int)dynamicOffsetX),
                                  staticOffsetY + rnd.nextInt((int)dynamicOffsetY));
        Point offset2 = new Point(staticOffsetX + rnd.nextInt((int)dynamicOffsetX),
                                  staticOffsetY + rnd.nextInt((int)dynamicOffsetY));

        Point pSrcLeftTop     = new Point(dx    , dy);
        Point pSrcRightTop    = new Point(dx + w, dy);
        Point pSrcLeftBottom  = new Point(dx    , dy + h);
        Point pSrcRightBottom = new Point(dx + w, dy + h);
        Mat src = new MatOfPoint2f(pSrcLeftTop    ,
                                   pSrcRightTop   ,
                                   pSrcLeftBottom ,
                                   pSrcRightBottom);

        boolean k = rnd.nextBoolean(); // false - left top   ; true - right top
        boolean m = rnd.nextBoolean(); // false - left bottom; true - right bottom

        Point pDstLeftTop     = new Point(pSrcLeftTop    .x - ( k ? offset1.x : 0),
                                          pSrcLeftTop    .y - ( k ? offset1.y : 0));
        Point pDstRightTop    = new Point(pSrcRightTop   .x + (!k ? offset1.x : 0),
                                          pSrcRightTop   .y - (!k ? offset1.y : 0));
        Point pDstLeftBottom  = new Point(pSrcLeftBottom .x - ( m ? offset2.x : 0),
                                          pSrcLeftBottom .y + ( m ? offset2.y : 0));
        Point pDstRightBottom = new Point(pSrcRightBottom.x + (!m ? offset2.x : 0),
                                          pSrcRightBottom.y + (!m ? offset2.y : 0));
        Mat dst = new MatOfPoint2f(
            pDstLeftTop,
            pDstRightTop,
            pDstLeftBottom,
            pDstRightBottom);

        Mat transformMatrix = Imgproc.getPerspectiveTransform(src, dst);

        BufferedImage imgTmp = addBorder(imgIn, Color.WHITE, new Size(dx * 2,
                                                                      dy * 2));
        Mat srcMat = ImgHelper.toMat(imgTmp);
        Mat dstMat = new Mat();
        Imgproc.warpPerspective(
            srcMat,
            dstMat,
            transformMatrix,
            new Size(0, 0),
            Imgproc.INTER_NEAREST,
            CvBorderTypes.BORDER_REPLICATE.getVal(),
            new Scalar(0xFF, 0xFF, 0xFF));

        BufferedImage imgOut = ImgHelper.toBufferedImage(dstMat);
        logger.info("Image perspective offsets: {} [{}]; {} [{}]",
                    k ? "left-top" : "right-top",
                    offset1,
                    m ? "left-bottom" : "right-bottom",
                    offset2);
        return imgOut;
    }

    private static void randomPerspective(File imgFileIn, File imgFileOut) throws IOException {
        logger.debug("imgFileIn.exist={}; imgFileOut.exist={}", imgFileIn.exists(), imgFileOut.exists());
        logger.debug("imgFileIn.path={}", imgFileIn.getAbsolutePath());
        if (!imgFileIn.exists())
            throw new IllegalArgumentException("Input file not found " + imgFileIn.getAbsolutePath());

        BufferedImage imgIn = ImageIO.read(imgFileIn);
        BufferedImage imgOut = randomRotate(imgIn);
        imgOut = randomPerspective(imgOut);

        save(imgOut, imgFileOut);
    }

    private static void save(BufferedImage imgOut, File imgFileOut) throws IOException {
        String ext = SelectFilterDialog.getExtension(imgFileOut);
        boolean succ = ImageIO.write(imgOut, ext, imgFileOut);
        if (!succ)
            logger.error("Can`t save image to {} file {}", ext, imgFileOut);
        Assertions.assertTrue(succ);
    }

    private static BufferedImage addBorder(BufferedImage img, Color fillColor, Size borderSize) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage outImg = new BufferedImage(
                w + (int)borderSize.width,
                h + (int)borderSize.height,
                img.getType());

        Graphics2D g = outImg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(fillColor);
        g.fillRect(0, 0, w + (int)borderSize.width, h + (int)borderSize.height);
        g.drawImage(img,
                (int)borderSize.width/2, (int)borderSize.height/2, w + (int)borderSize.width/2, h + (int)borderSize.height/2,
                0, 0, w, h,
                fillColor, null);
        g.dispose();

        return outImg;
    }

    /** Make demo image for pipeline ./exampleImages/idCard.LeadToHorizont.json */
    @Test
    public void randomRotateTest() throws IOException {
        randomRotate(
                Paths.get("exampleImages", "passportUkr.jpg"    ).toFile(),
                Paths.get("exampleImages", "passportUkr_rotated.png").toFile());
    }

    /** Make demo image for pipeline ./exampleImages/idCard.LeadToPerspective.json */
    @Test
    public void randomPerspectiveTest() throws IOException {
        randomPerspective(
                Paths.get("exampleImages", "passportUkr.jpg"          ).toFile(),
                Paths.get("exampleImages", "passportUkr_perspctve.png").toFile());
    }

    public static void main(String[] args) {
        try {
            new PassportUkrTest().randomRotateTest();
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

    }

}
