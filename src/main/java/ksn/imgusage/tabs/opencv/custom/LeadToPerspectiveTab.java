package ksn.imgusage.tabs.opencv.custom;

import java.awt.Component;
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
        Point targetPoint;
        ETargetPoint target;
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
    private List<IterationResult> allIterations = new ArrayList<>();
    private int bestIndex;
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
        allIterations.clear();
        Size sizeSrc = imageMat.size();
        IterationResult started = findMaxContourArea(imageMat, sizeSrc.width * sizeSrc.height, logger);;
        allIterations.add(started);
        bestIndex = 0;
        imageMat = started.mat;

        SwingUtilities.invokeLater(this::nextIteration);
    }

    private void nextIteration() {
        if (getSourceMat() == null)
            return;

        try {
            nextIterationRightTopX(+115);
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

    private void nextIterationRightTopX(int offxsetX) {
        if (getSourceMat() == null)
            return;

        IterationResult started = allIterations.get(0);
        Rect rc = started.rcOut;
        Point rightTop = new Point(rc.x + rc.width, rc.y);
        Point targetPoint = new Point(rightTop.x + offxsetX, rightTop.y);

        boolean repeat;
        if (targetPoint.x >= imageMat.width()*1.5) {
            repeat = false;
        } else {
          //IterationResult prev = allIterations.get(allIterations.size() - 1);
            IterationResult curr = tryPerspectiveAndFindMaxContourArea(targetPoint, ETargetPoint.RIGHT_TOP, RED);
            boolean skip = curr.rcOut == null;
            if (skip) {
                repeat = true;
            } else {
                allIterations.add(curr);
                // show intermediate result
                applyImage(curr.mat);

                boolean sameHeight = 4 < Math.abs(rc.height - curr.rcOut.height);
                boolean betterByArea = curr.area <= started.area;
                boolean better = betterByArea && sameHeight;
                if (better)
                    bestIndex = allIterations.size() - 1;
                repeat = better;
            }
        }

        if (repeat)
            SwingUtilities.invokeLater(() -> this.nextIterationRightTopX(offxsetX + 1));
        else
            SwingUtilities.invokeLater(() -> this.nextIterationRightTopY(-1));
            //SwingUtilities.invokeLater(() -> this.nextIterationRightBottomX(+1));
    }

    private void nextIterationRightTopY(int offxsetY) {
        if (getSourceMat() == null)
            return;

        IterationResult started = allIterations.get(0);
        Rect rc = started.rcOut;
        Point rightTop = new Point(rc.x + rc.width, rc.y);
        Point targetPoint = new Point(rightTop.x, rightTop.y + offxsetY);

        boolean repeat;
        if (targetPoint.y <= -imageMat.height()/2) {
            repeat = false;
        } else {
          //IterationResult prev = allIterations.get(allIterations.size() - 1);
            IterationResult curr = tryPerspectiveAndFindMaxContourArea(targetPoint, ETargetPoint.RIGHT_TOP, RED);
            boolean skip = curr.rcOut == null;
            if (skip) {
                repeat = true;
            } else {
                allIterations.add(curr);
                // show intermediate result
                applyImage(curr.mat);

                boolean sameWidth = 4 < Math.abs(rc.width - curr.rcOut.width);
                boolean betterByArea = curr.area <= started.area;
                boolean better = betterByArea && sameWidth;
                if (better)
                    bestIndex = allIterations.size() - 1;
                repeat = better;
            }
        }

        if (repeat)
            SwingUtilities.invokeLater(() -> this.nextIterationRightTopY(offxsetY - 1));
        else
            SwingUtilities.invokeLater(() -> this.nextIterationRightBottomX(+1));
    }

    private void nextIterationRightBottomX(int offsetX) {
        // TODO
        //showBest();
    }

    private void showBest() {
        IterationResult bestResult = allIterations.get(bestIndex);
        IterationResult bestResultGreen = tryPerspectiveAndFindMaxContourArea(bestResult.targetPoint, bestResult.target, GREEN);
        // show best result
        applyImage(bestResultGreen.mat);
    }

    enum ETargetPoint {
        RIGHT_TOP,
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_BOTTOM
    }

    private IterationResult tryPerspectiveAndFindMaxContourArea(Point targetPoint, ETargetPoint target, Scalar rcColor) {
        IterationResult started = allIterations.get(0);
        Rect srcRc = started.rcOut;
        Point rcLeftTop     = new Point(srcRc.x              , srcRc.y               );
        Point rcRightTop    = new Point(srcRc.x + srcRc.width, srcRc.y               );
        Point rcLeftBottom  = new Point(srcRc.x              , srcRc.y + srcRc.height);
        Point rcRightBottom = new Point(srcRc.x + srcRc.width, srcRc.y + srcRc.height);
        Mat src = new MatOfPoint2f(rcLeftTop    ,
                                   rcRightTop   ,
                                   rcLeftBottom ,
                                   rcRightBottom);
        Mat dst = new MatOfPoint2f(
            (target == ETargetPoint.LEFT_TOP    ) ? targetPoint : rcLeftTop,
            (target == ETargetPoint.RIGHT_TOP   ) ? targetPoint : rcRightTop,
            (target == ETargetPoint.LEFT_BOTTOM ) ? targetPoint : rcLeftBottom,
            (target == ETargetPoint.RIGHT_BOTTOM) ? targetPoint : rcRightBottom);
        Mat transformMatrix = Imgproc.getPerspectiveTransform(src, dst);

        Mat dst2 = new Mat();
        Imgproc.warpPerspective(
            imageMat, // src
            dst2,
            transformMatrix,
            new Size(0, 0),
            Imgproc.INTER_NEAREST);


        Size sizeSrc = imageMat.size();
        IterationResult res = findMaxContourArea(dst2, sizeSrc.width * sizeSrc.height, logger);
        if ((res.rcOut != null) && (rcColor != null)) {
            Imgproc.rectangle(res.mat,
                new Point(res.rcOut.x, res.rcOut.y),
                new Point(res.rcOut.x + res.rcOut.width, res.rcOut.y + res.rcOut.height),
                rcColor,
                1);
        }
        res.target = target;
        res.targetPoint = targetPoint;
        return res;
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
