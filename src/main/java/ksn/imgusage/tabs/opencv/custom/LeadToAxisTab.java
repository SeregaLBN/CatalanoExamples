package ksn.imgusage.tabs.opencv.custom;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.*;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.custom.LeadToAxisTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** Find the optimal contour for binding to the axis X/Y (horizontal or vertical) */
public class LeadToAxisTab extends CustomTab<LeadToAxisTabParams> {

    public static final String TAB_TITLE = "LeadToAxis";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal contour for binding to the axis X/Y (horizontal or vertical)";

    public  static final int ANGLE_LEAD_MIN = -360;
    public  static final int ANGLE_LEAD_MAX = +360;
    private static final int ANGLE_LEAD_MIN_DIFF = 10;

    private static class IterationResult {
        final Mat mat;
        final double angle;
        final double area;
        final Rect rcOut;
        IterationResult(Mat mat, double angle, double area, Rect rc) {
            this.mat = mat;
            this.angle = angle;
            this.area = area;
            this.rcOut = rc;
        }
        @Override
        public String toString() {
            return "{mat=["+mat.width()+"x"+mat.height()+"], angle=" + angle + ", area=" + area + ", rcOut=" + rcOut + "}";
        }
    }

    private LeadToAxisTabParams params;
    private Mat matStarted;
    private List<IterationResult> results = new ArrayList<>();
    private Consumer<String> showResultAngle;

    @Override
    public Component makeTab(LeadToAxisTabParams params) {
        if (params == null)
            params = new LeadToAxisTabParams();

        params.angleRangeMin = Math.max(ANGLE_LEAD_MIN                      , Math.min(ANGLE_LEAD_MAX - ANGLE_LEAD_MIN_DIFF, params.angleRangeMin));
        params.angleRangeMax = Math.max(ANGLE_LEAD_MIN + ANGLE_LEAD_MIN_DIFF, Math.min(ANGLE_LEAD_MAX                      , params.angleRangeMax));
        if (params.angleRangeMin > (params.angleRangeMax - ANGLE_LEAD_MIN_DIFF))
            params.angleRangeMin =  params.angleRangeMax - ANGLE_LEAD_MIN_DIFF;

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
        matStarted = imageMat;
        showResultAngle.accept(".?.");

        SwingUtilities.invokeLater(() -> this.nextIteration(params.angleRangeMin));
    }

    private void applyImage(Mat mat) {
        imageMat = mat;
        image = ImgHelper.toBufferedImage(imageMat);
        imagePanelRepaint.run();
        tabHandler.onImageChanged(this);
    }

    private void nextIteration(double angle) {
        if (nextIteration2(angle)) {
            // show intermediate result
            IterationResult last = results.get(results.size() - 1);
            applyImage(last.mat);

            SwingUtilities.invokeLater(() -> this.nextIteration(angle + 1.0));
        } else {
            // show final result
            if (results.isEmpty()) {
                // nothing found (
                applyImage(matStarted);
                showResultAngle.accept(":(");
            } else {
                // show best result
                IterationResult best = results.get(0);
                best = rotateAndFindMaxContourArea(best.angle, new Scalar(0, 255, 0)); // optional; another rectangle color
                applyImage(best.mat);
                showResultAngle.accept(String.format(Locale.US, "%.2f", best.angle));
            }
        }
    }

    private boolean nextIteration2(double angle) {
        if (angle > params.angleRangeMax) {
            findBestIteration();
            return false; // stop iterations
        } else {
            IterationResult resIter = rotateAndFindMaxContourArea(angle, new Scalar(0, 0, 255));
            logger.trace("nextIteration: {}", resIter);
            results.add(resIter);
            return true; // need next iteration
        }
    }

    private void findBestIteration() {
        logger.trace("findBestIteration: total.size={}", results.size());

        List<IterationResult> valid = results.stream()
                .filter(item -> item.area > 0)
                .filter(item -> item.rcOut != null)
                .filter(item ->
                    params.leadToHorizontal
                        ? item.rcOut.width >= item.rcOut.height
                        : item.rcOut.width <  item.rcOut.height)
                .collect(Collectors.toList());
        logger.trace("findBestIteration: valid.size={}", valid.size());

        // find median of area values
        double[] areas = valid.stream()
            .mapToDouble(item -> item.area)
            .toArray();
        double medianArea = new Median().evaluate(areas);
        logger.trace("findBestIteration: medianArea={}", medianArea);

        // leave not exceeding the median by X percent
        List<IterationResult> best = valid.stream()
            .filter(item -> {
                if (params.limitAreaDiffInPercent > 100)
                    return true; // all
                double percent = item.area * 100 / medianArea;
                return Math.abs(100 - percent) <= (params.limitAreaDiffInPercent + 1);
            })
            .collect(Collectors.toList());
        logger.trace("findBestIteration: medianed.size={}", best.size());

        // sort by rcOut area
        best.sort((item1, item2) -> {
            int area1 = item1.rcOut.width * item1.rcOut.height;
            int area2 = item2.rcOut.width * item2.rcOut.height;
            if (area1 > area2)
                return +1;
            if (area1 < area2)
                return -1;
            return 0;
        });

        if (best.isEmpty()) {
            logger.warn("findBestIteration: Nothing found (");
            results.clear();
            return;
        }

        // get ONE best
        IterationResult one = best.get(0);
        logger.trace("findBestIteration: finded {}", one);

        // set result
        results.clear();
        results.add(one);
    }

    private IterationResult rotateAndFindMaxContourArea(double angle, Scalar rcColor) {
        Point center = new Point(matStarted.width() / 2.0, matStarted.height() / 2.0);
        Mat rotateMatrix = Imgproc.getRotationMatrix2D(center, angle, 1);

        Mat dst = new Mat(matStarted.rows(), matStarted.cols(), matStarted.type());
        Imgproc.warpAffine(
                matStarted,
                dst,
                rotateMatrix,
                new org.opencv.core.Size(0, 0),
                Imgproc.INTER_LINEAR);

        IterationResult res = findMaxContourArea(angle, dst);
        if (res.rcOut != null) {
            Imgproc.rectangle(res.mat,
                new Point(res.rcOut.x, res.rcOut.y),
                new Point(res.rcOut.x + res.rcOut.width, res.rcOut.y + res.rcOut.height),
                rcColor,
                1);
        }
        return res;
    }

    private IterationResult findMaxContourArea(double angle, Mat imageSrc) {
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
            return new IterationResult(imageSrc, angle, -1, null);
        }

        double areaLimit = matStarted.width() * matStarted.height() / 3.5;
        IterationResult pairMaxArea = contours.stream()
            .map(contour -> {
                double area = Math.abs(Imgproc.contourArea(contour));
                if (area < areaLimit)
                    return null;
                return new IterationResult(null, angle, area, Imgproc.boundingRect(contour));
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
            return new IterationResult(imageSrc, angle, -1, null);
        }
        return new IterationResult(imageSrc, angle, pairMaxArea.area, pairMaxArea.rcOut);
    }

    @Override
    protected Component makeOptions() {
        SliderIntModel modelLimitAreaDiffInPercent = new SliderIntModel(params.limitAreaDiffInPercent, 0, 0, 101);
        SliderIntModel modelAngleRangeMin = new SliderIntModel(params.angleRangeMin, 0, ANGLE_LEAD_MIN, ANGLE_LEAD_MAX);
        SliderIntModel modelAngleRangeMax = new SliderIntModel(params.angleRangeMax, 0, ANGLE_LEAD_MIN, ANGLE_LEAD_MAX);

        JButton btnRepeat = new JButton("Repeat...");
        btnRepeat.addActionListener(ev -> resetImage());

        Box boxLeadToHorizont = makeBoxedCheckBox(
            () -> params.leadToHorizontal,
            v  -> params.leadToHorizontal = v,
            "",
            "Lead to horizontal",
            "params.leadToHorizontal",
            null, null);

        Box box4AngleRange = Box.createHorizontalBox();
        box4AngleRange.setBorder(BorderFactory.createTitledBorder("Range angles"));
        box4AngleRange.add(makeSliderVert(modelAngleRangeMin, "min", null));
        box4AngleRange.add(Box.createHorizontalStrut(2));
        box4AngleRange.add(makeSliderVert(modelAngleRangeMax, "max", null));

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.add(makeSliderVert(modelLimitAreaDiffInPercent, "Median diff", "leave not exceeding the median by X percent"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(box4AngleRange);

        List<Consumer<String>> setterTextList = new ArrayList<>(1);
        Container cntrlToShowResult = makeEditBox(setterTextList::add, null, "Angle found", "Result", "Best turning angle");
        showResultAngle = text -> setterTextList.get(0).accept(text == null ? "" : text);

        addChangeListener("modelLimitAreaDiffInPercent", modelLimitAreaDiffInPercent, v -> params.limitAreaDiffInPercent = v);
        addChangeListener("modelAngleRangeMin", modelAngleRangeMin, v -> params.angleRangeMin = v, () -> {
            if (params.angleRangeMin > (params.angleRangeMax - ANGLE_LEAD_MIN_DIFF))
                SwingUtilities.invokeLater(() -> modelAngleRangeMax.setValue(params.angleRangeMin + ANGLE_LEAD_MIN_DIFF));
        });
        addChangeListener("modelAngleRangeMax", modelAngleRangeMax, v -> params.angleRangeMax = v, () -> {
            if (params.angleRangeMax < (params.angleRangeMin + ANGLE_LEAD_MIN_DIFF))
                SwingUtilities.invokeLater(() -> modelAngleRangeMin.setValue(params.angleRangeMax - ANGLE_LEAD_MIN_DIFF));
        });

        JPanel panelOption = new JPanel();
        panelOption.setLayout(new BorderLayout());
        panelOption.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOption.add(boxLeadToHorizont, BorderLayout.NORTH);
        panelOption.add(box4Sliders      , BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(""));
        panel.add(btnRepeat        , BorderLayout.NORTH);
        panel.add(panelOption      , BorderLayout.CENTER);
        panel.add(cntrlToShowResult, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public LeadToAxisTabParams getParams() {
        return params;
    }

}
