package ksn.imgusage.tabs.another;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.another.LeadToHorizontalTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** Find the optimal contour for binding to the horizontal */
public class LeadToHorizontalTab extends AnotherTab<LeadToHorizontalTabParams> {

    public static final String TAB_TITLE = "LeadToHorizontal";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal contour for binding to the horizontal";

    public static final double ANGLE_LEAD_MIN = -60;
    public static final double ANGLE_LEAD_MAX = +60;

    private LeadToHorizontalTabParams params;
    private Mat matStarted;
    private double angleIteration;
    private List<IterationResult> results = new ArrayList<>();

    @Override
    public Component makeTab(LeadToHorizontalTabParams params) {
        if (params == null)
            params = new LeadToHorizontalTabParams();
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
        results.clear();
        angleIteration = ANGLE_LEAD_MIN;
        matStarted = imageMat;

        SwingUtilities.invokeLater(this::nextIteration);
    }

    private void nextIteration() {
        Consumer<Mat> setImage = mat -> {
            imageMat = mat;
            image = ImgHelper.toBufferedImage(imageMat);
            imagePanelRepaint.run();
            tabHandler.onImageChanged(this);
        };
        if (angleIteration > ANGLE_LEAD_MAX) {
            logger.trace("nextIteration: show lead: angleLead={}", angleLead);

            IterationResult resLead = rotateAndFindMaxContourArea(angleLead, new Scalar(0, 255, 0));
            setImage.accept(resLead.mat);
        } else {
            IterationResult resIter = rotateAndFindMaxContourArea(angleIteration, new Scalar(0, 0, 255));
            logger.trace("nextIteration: {}", resIter);
            if (resIter.rcOut != null)
                results.add(resIter);

            angleIteration += 1.0;
            SwingUtilities.invokeLater(this::nextIteration);
        }
    }

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

    private IterationResult rotateAndFindMaxContourArea(double angle, Scalar rcColor) {
        Point center = new Point(matStarted.width() / 2, matStarted.height() / 2);
        Mat rotateMatrix = Imgproc.getRotationMatrix2D(
                center,
                angle,
                1);

        Mat dst = new Mat(matStarted.rows(), matStarted.cols(), matStarted.type());
        Imgproc.warpAffine(
                matStarted,
                dst,
                rotateMatrix,
                new org.opencv.core.Size(0, 0),
                Imgproc.INTER_LINEAR);

        IterationResult res = findMaxContourArea(dst);
        if (res.rcOut != null) {
            Imgproc.rectangle(res.mat,
                new Point(res.rcOut.x, res.rcOut.y),
                new Point(res.rcOut.x + res.rcOut.width, res.rcOut.y + res.rcOut.height),
                rcColor,
                1);
        }
        return res;
    }

    private IterationResult findMaxContourArea(Mat imageSrc) {
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

        double areaLimit = matStarted.width() * matStarted.height() / 3.5;
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
    public LeadToHorizontalTabParams getParams() {
        return params;
    }

}
