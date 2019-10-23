package ksn.imgusage.tabs.opencv.custom;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;

import ksn.imgusage.type.dto.opencv.custom.LeadToPerspectiveTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** Find the optimal perspective binded to an existing outer rectangle */
public class LeadToPerspectiveTab extends CustomTab<LeadToPerspectiveTabParams> {

    public static final String TAB_TITLE = "LeadToPerspective";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal perspective binded to an existing outer rectangle";

                                                   // B    G    R
    private static final Scalar YELLOW = new Scalar(0x00, 0xFF, 0xFF);
    private static final Scalar RED    = new Scalar(0x00, 0x00, 0xFF);
    private static final Scalar GREEN  = new Scalar(0x00, 0xFF, 0x00);

    /** Contour info */
    private static class IterationResult {
        final Mat mat;
        final double area;
        final Rect rcOut;

        IterationResult(Mat mat, double area, Rect rc) {
            this.mat = mat;
            this.area = area;
            this.rcOut = rc;
        }
        @Override
        public String toString() {
            return "{mat=["+mat.width()+"x"+mat.height()+"], area=" + area + ", rcOut=" + rcOut + "}";
        }
    }

    private LeadToPerspectiveTabParams params;
    private IterationResult started;
    private IterationResult last;
    private boolean isBestFound;
    private final Point offsetRT = new Point();
    private final Point offsetRB = new Point();
    private final Point offsetLT = new Point();
    private final Point offsetLB = new Point();

    @Override
    public BufferedImage getDrawImage() {
        Scalar color;
        IterationResult res = last;
        if (res == null) {
            res = started;
            color = YELLOW;
        } else {
            color = isBestFound ? GREEN : RED;
        }
        if (res != null && res.rcOut != null) {
            Mat copy = res.mat.clone();
            Imgproc.rectangle(
                copy,
                res.rcOut.tl(),
                res.rcOut.br(),
                color,
                1);
            return ImgHelper.toBufferedImage(copy);
        }

        return super.getDrawImage();
    }

    @Override
    public Component makeTab(LeadToPerspectiveTabParams params) {
        if (params == null)
            params = new LeadToPerspectiveTabParams();
        this.params = params;

        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void applyOpencvFilter() {
        isBestFound = false;
        offsetRT.x = offsetRT.y = 0;
        offsetRB.x = offsetRB.y = 0;
        offsetLT.x = offsetLT.y = 0;
        offsetLT.x = offsetLT.y = 0;
        Size sizeSrc = imageMat.size();
        started = findMaxContourArea(imageMat, sizeSrc.width * sizeSrc.height, logger);
        logger.trace("applyOpencvFilter: iterationResult: rc={}, area={}", started.rcOut, started.area);
        imageMat = started.mat;

        if (started.rcOut == null)
            tabHandler.onError(new Exception("Not found started contour"), this, null);
        else
            SwingUtilities.invokeLater(this::nextIteration);
    }

    private void nextIteration() {
        if (getSourceMat() == null)
            return;

        try {
            nextIterationRightTopX(+1);
        } catch (Exception ex) {
            logger.error("nextIteration: {}", ex);
            tabHandler.onError(ex, this, null);
        }
    }

    private void applyImage(Mat mat) {
        imageMat = mat;
        image = ImgHelper.toBufferedImage(imageMat);
        imagePanelRepaint.run();
        tabHandler.onImageChanged(this);
    }

    private void nextIterationRightTopX(int offsetX) {
        if (getSourceMat() == null)
            return;

        logger.trace("nextIterationRightTopX: offxsetX={}", offsetX);

        Rect rc = started.rcOut;
        Point rightTop = new Point(rc.x + rc.width, rc.y);
        Point offset = new Point(offsetX, offsetRT.y);

        boolean repeat;
        if ((rightTop.x + offset.x) >= imageMat.width()*1.5) {
            repeat = false;
        } else {
            last = tryPerspectiveAndFindMaxContourArea(offset, ETargetPoint.RIGHT_TOP, RED);
            logger.trace("nextIterationRightTopX: iterationResult: rc={}, area={}", last.rcOut, last.area);

            // show intermediate result
            applyImage(last.mat);

            boolean skip = last.rcOut == null;
            if (skip) {
                repeat = true;
            } else {
                boolean sameHeight = 4 < Math.abs(rc.height - last.rcOut.height);
                boolean betterByArea = last.area <= started.area;
                boolean better = betterByArea && sameHeight;
                if (better)
                    offsetRT.x = offsetX;
                repeat = better;
            }
        }

        if (repeat)
            SwingUtilities.invokeLater(() -> this.nextIterationRightTopX(offsetX + 1));
//        else
//            SwingUtilities.invokeLater(() -> this.nextIterationRightTopY(-1));
//            SwingUtilities.invokeLater(() -> this.nextIterationRightBottomX(+1));
    }

    private void nextIterationRightTopY(int offsetY) {
        if (getSourceMat() == null)
            return;

        logger.trace("nextIterationRightTopY: offxsetY={}", offsetY);

        Rect rc = started.rcOut;
        Point rightTop = new Point(rc.x + rc.width, rc.y);
        Point offset = new Point(offsetRT.x, offsetY);

        boolean repeat;
        if ((rightTop.y + offset.y) <= -imageMat.height()/2) {
            repeat = false;
        } else {
            last = tryPerspectiveAndFindMaxContourArea(offset, ETargetPoint.RIGHT_TOP, RED);
            logger.trace("nextIterationRightTopY: iterationResult: rc={}, area={}", last.rcOut, last.area);

            // show intermediate result
            applyImage(last.mat);

            boolean skip = last.rcOut == null;
            if (skip) {
                repeat = true;
            } else {

                boolean sameWidth = 4 < Math.abs(rc.width - last.rcOut.width);
                boolean betterByArea = last.area <= started.area;
                boolean better = betterByArea && sameWidth;
                if (better)
                    offsetRT.y = offsetY;
                repeat = better;
            }
        }

        if (repeat)
            SwingUtilities.invokeLater(() -> this.nextIterationRightTopY(offsetY - 1));
        else
            SwingUtilities.invokeLater(() -> this.nextIterationRightBottomX(+1));
    }

    private void nextIterationRightBottomX(int offsetX) {
        // TODO
        showBest();
    }

    private void showBest() {
        isBestFound = true;
        IterationResult bestResultGreen = tryPerspectiveAndFindMaxContourArea(null, null, GREEN);
        // show best result
        applyImage(bestResultGreen.mat);
//        applyImage(started.mat);
    }

    enum ETargetPoint {
        RIGHT_TOP,
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_BOTTOM
    }

    private IterationResult tryPerspectiveAndFindMaxContourArea(Point offset, ETargetPoint target, Scalar rcColor) {
        Rect rcSrc = started.rcOut;
        Point pSrcLeftTop     = new Point(rcSrc.x              , rcSrc.y               );
        Point pSrcRightTop    = new Point(rcSrc.x + rcSrc.width, rcSrc.y               );
        Point pSrcLeftBottom  = new Point(rcSrc.x              , rcSrc.y + rcSrc.height);
        Point pSrcRightBottom = new Point(rcSrc.x + rcSrc.width, rcSrc.y + rcSrc.height);
        Mat src = new MatOfPoint2f(pSrcLeftTop    ,
                                   pSrcRightTop   ,
                                   pSrcLeftBottom ,
                                   pSrcRightBottom);

        Point pDstLeftTop     = new Point(pSrcLeftTop    .x + ((target == ETargetPoint.LEFT_TOP    ) ? offset.x : offsetLT.x),
                                          pSrcLeftTop    .y + ((target == ETargetPoint.LEFT_TOP    ) ? offset.y : offsetLT.y));
        Point pDstRightTop    = new Point(pSrcRightTop   .x + ((target == ETargetPoint.RIGHT_TOP   ) ? offset.x : offsetRT.x),
                                          pSrcRightTop   .y + ((target == ETargetPoint.RIGHT_TOP   ) ? offset.y : offsetRT.y));
        Point pDstLeftBottom  = new Point(pSrcLeftBottom .x + ((target == ETargetPoint.LEFT_BOTTOM ) ? offset.x : offsetLB.x),
                                          pSrcLeftBottom .y + ((target == ETargetPoint.LEFT_BOTTOM ) ? offset.y : offsetLB.y));
        Point pDstRightBottom = new Point(pSrcRightBottom.x + ((target == ETargetPoint.RIGHT_BOTTOM) ? offset.x : offsetRB.x),
                                          pSrcRightBottom.y + ((target == ETargetPoint.RIGHT_BOTTOM) ? offset.y : offsetRB.y));
        Mat dst = new MatOfPoint2f(
            pDstLeftTop,
            pDstRightTop,
            pDstLeftBottom,
            pDstRightBottom);

        Mat transformMatrix = Imgproc.getPerspectiveTransform(src, dst);

        Mat dst2 = new Mat();
        Imgproc.warpPerspective(
            imageMat, // src
            dst2,
            transformMatrix,
            new Size(0, 0),
            Imgproc.INTER_NEAREST);


        Size sizeSrc = imageMat.size();
        return findMaxContourArea(dst2, sizeSrc.width * sizeSrc.height, logger);
    }

    static IterationResult findMaxContourArea(Mat imageSrc, double originalMaxArea, Logger logger) {
        // cast to gray image
        imageSrc = OpenCvHelper.toGray(imageSrc);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageSrc,  // src
            contours,  // out1
            hierarchy, // out2
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE);

        { // restore color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageSrc, mat, Imgproc.COLOR_GRAY2RGB);
            imageSrc = mat;
        }

        if (contours.isEmpty()) {
            logger.warn("findOuterRect: No any contours found!");
            return new IterationResult(imageSrc, -1, null);
        }

        double areaLimit = originalMaxArea / 3.5;
        IterationResult pairMaxArea = contours.stream()
            .map(contour -> {
                double area = Math.abs(Imgproc.contourArea(contour));
                if (area < areaLimit)
                    return null;
                return new IterationResult(null, area, Imgproc.boundingRect(contour));
            })
            .filter(Objects::nonNull)
            .max((item1, item2) -> {
                if (item1.area > item2.area)
                    return +1;
                if (item1.area < item2.area)
                    return -1;
                return 0;
            })
            .orElseGet(() -> null);
        if (pairMaxArea == null) {
            logger.warn("findOuterRect: No matching contour found");
            return new IterationResult(imageSrc, -1, null);
        }
        return new IterationResult(imageSrc, pairMaxArea.area, pairMaxArea.rcOut);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        JButton btnRepeat = new JButton("Repeat...");
        btnRepeat.addActionListener(ev -> resetImage());
        box4Options.add(btnRepeat);

        return box4Options;
    }

    @Override
    public LeadToPerspectiveTabParams getParams() {
        return params;
    }

}
