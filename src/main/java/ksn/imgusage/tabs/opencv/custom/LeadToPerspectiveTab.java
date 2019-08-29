package ksn.imgusage.tabs.another;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.another.LeadToPerspectiveTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** Find the optimal perspective binded to an existing outer rectangle */
public class LeadToPerspectiveTab extends AnotherTab<LeadToPerspectiveTabParams> {

    public static final String TAB_TITLE = "LeadToPerspective";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal perspective binded to an existing outer rectangle";

    public static final double ANGLE_LEAD_MIN = -120;
    public static final double ANGLE_LEAD_MAX = +120;

    private LeadToPerspectiveTabParams params;
    private Mat matStarted;
    private Double angleLead = null;
    private Rect rcLead = null;
    private double angleIteration;

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
        angleIteration = ANGLE_LEAD_MIN;
        matStarted = imageMat;
        angleLead = null;
        rcLead = null;

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

            Pair resLead = rotate(angleLead, new Scalar(0, 255, 0));
            setImage.accept(resLead.mat);
        } else {
            Pair resIter = rotate(angleIteration, new Scalar(0, 0, 255));
            if (rcLead == null) {
                angleLead = angleIteration;
                rcLead = resIter.rc;
                logger.trace("nextIteration: init: angleLead={}, rcLead={}", angleLead, rcLead);
                setImage.accept(resIter.mat);
            } else {
                if (resIter.rc != null) {
                    double areaLead = rcLead.width * rcLead.height;
                    double areaIter = resIter.rc.width * resIter.rc.height;
                    if (areaIter < areaLead) {
                        angleLead = angleIteration;
                        rcLead = resIter.rc;
                        logger.trace("nextIteration: minArea found: area={}, angleLead={}, rcLead={}", areaIter, angleLead, rcLead);
                        setImage.accept(resIter.mat);
                    }
                }
            }
            angleIteration += 1.0;
            SwingUtilities.invokeLater(this::nextIteration);
        }
    }

    private static class Pair {
        final Mat mat;
        final Rect rc;
        Pair(Mat mat, Rect rc) {
            this.mat = mat;
            this.rc = rc;
        }
    }

    private Pair rotate(double angle, Scalar rcColor) {
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

        Pair res = findOuterRect(dst);
        if (res.rc != null) {
            Imgproc.rectangle(res.mat,
                new Point(res.rc.x, res.rc.y),
                new Point(res.rc.x + res.rc.width, res.rc.y + res.rc.height),
                rcColor,
                1);
        }
        return res;
    }

    private Pair findOuterRect(Mat imageSrc) {
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
            return new Pair(imageSrc, null);
        }

        int maxW = imageSrc.width()  / 3;
        int maxH = imageSrc.height() / 3;
        Rect rcRes = contours.stream()
            .map(Imgproc::boundingRect)
            .filter(rc -> rc.width >= maxW && rc.height > maxH)
            .filter(rc -> rc.width >=rc.height)
            .max((rc1, rc2) -> {
                int area1 = rc1.width * rc1.height;
                int area2 = rc2.width * rc2.height;
                if (area1 > area2)
                    return +1;
                if (area1 < area2)
                    return -1;
                return 0;
            })
            .orElseGet(() -> null);
        if (rcRes == null)
            logger.warn("findOuterRect: No matching contour found");
        return new Pair(imageSrc, rcRes);
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
