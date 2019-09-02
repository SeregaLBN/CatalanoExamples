package ksn.imgusage.tabs.opencv.custom;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
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
import org.slf4j.Logger;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.custom.LeadToAxisTabParams;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.OpenCvHelper;

/** Find the optimal contour for binding to the axis X/Y (horizontal or vertical) */
public class LeadToAxisTab extends CustomTab<LeadToAxisTabParams> {

    public static final String TAB_TITLE = "LeadToAxis";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Find the optimal contour for binding to the axis X/Y (horizontal or vertical)";

    public  static final int ANGLE_LEAD_MIN = -180;
    public  static final int ANGLE_LEAD_MAX = +180;
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
    private List<IterationResult> results = new ArrayList<>();
    private BufferedImage best;
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
    public BufferedImage getDrawImage() {
        if (best != null)
            return best;
        return super.getDrawImage();
    }

    @Override
    protected void applyOpencvFilter() {
        results.clear();
        best = null;
        showResultAngle.accept("???");

        int max = Math.max(imageMat.width(), imageMat.height());
        int diagonal = (int)Math.sqrt(max * max * 2);
        Mat matStarted = new Mat(diagonal, diagonal, imageMat.type(), new Scalar(0,0,0));
        int offsetX = (diagonal - imageMat.width()) / 2;
        int offsetY = (diagonal - imageMat.width()) / 2;
        imageMat.copyTo(matStarted.colRange(offsetX, offsetX + imageMat.width())
                                  .rowRange(offsetY, offsetY + imageMat.height()));

        SwingUtilities.invokeLater(() -> this.nextIteration(matStarted, params.angleRangeMin));
    }

    private void applyImage(Mat mat) {
        imageMat = mat;
        image = ImgHelper.toBufferedImage(imageMat);
        imagePanelRepaint.run();
        tabHandler.onImageChanged(this);
    }

    private void nextIteration(Mat matStarted, double angle) {
        try {
            nextIteration1(matStarted, angle);
        } catch (Exception ex) {
            logger.error("nextIteration: {}", ex);
            tabHandler.onError(ex.getMessage(), this, null);
        }
    }
    private void nextIteration1(Mat matStarted, double angle) {
        if (nextIteration2(matStarted, angle)) {
            // show intermediate result
            IterationResult last = results.get(results.size() - 1);
            applyImage(last.mat);

            SwingUtilities.invokeLater(() -> this.nextIteration(matStarted, angle + 1.0));
        } else {
            // show final result
            showFinalResult(matStarted);
        }
    }

    private void showFinalResult(Mat matStarted) {
        if (results.isEmpty()) {
            // nothing found (
            applyImage(getSourceMat());
            showResultAngle.accept(":(");

            return;
        }

        // show best result
        IterationResult best = results.get(0);
        best = rotateAndFindMaxContourArea(matStarted, best.angle, new Scalar(0, 255, 0)); // optional; another rectangle color

        Size sizeSrc = getSourceMat().size();
        Mat dst = new Mat(sizeSrc, best.mat.type(), new Scalar(0,0,0));
        Rect roi = best.rcOut;
        roi.width++; roi.height++; // add color border :(
        if (!params.keepSourceSize) {
            if (params.cutBorders)
                new Mat(best.mat, roi).copyTo(dst);
            else
                dst = best.mat;
        } else {
            // restore original size
            if ((sizeSrc.width < best.rcOut.width) || (sizeSrc.height < best.rcOut.height)) {

                logger.trace("src=[{}x{}]", sizeSrc.width, sizeSrc.height);
                logger.trace("roi=[{}x{}]", roi.width, roi.height);
                double zoomX = sizeSrc.width  / roi.width;
                double zoomY = sizeSrc.height / roi.height;
                double zoom = Math.min(zoomX, zoomY);

                int newRoiWidth  = (int)(roi.width  * zoom);
                int newRoiHeight = (int)(roi.height * zoom);
                logger.trace("roiZoom=[{}x{}]", newRoiWidth, newRoiHeight);

                Mat resized = new Mat();
                Imgproc.resize(new Mat(best.mat, roi), resized, new Size(newRoiWidth, newRoiHeight));
                logger.trace("resizedMat=[{}x{}]", resized.width(), resized.height());

                int offsetX = ((int)sizeSrc.width  - newRoiWidth ) / 2;
                int offsetY = ((int)sizeSrc.height - newRoiHeight) / 2;
                logger.trace("offset=[{}x{}]", offsetX, offsetY);
                resized.copyTo(dst.colRange(offsetX, offsetX + newRoiWidth)
                                  .rowRange(offsetY, offsetY + newRoiHeight));
            } else {
                int offsetX = (dst.width()  - roi.width ) / 2;
                int offsetY = (dst.height() - roi.height) / 2;
                new Mat(best.mat, roi).copyTo(dst.colRange(offsetX, offsetX + roi.width)
                                                 .rowRange(offsetY, offsetY + roi.height));
            }
        }
        applyImage(dst);

        showResultAngle.accept(String.format(Locale.US, "%.2f", best.angle));
    }

    private boolean nextIteration2(Mat matStarted, double angle) {
        if (angle > params.angleRangeMax) {
            findBestIteration();
            return false; // stop iterations
        } else {
            IterationResult resIter = rotateAndFindMaxContourArea(matStarted, angle, new Scalar(0, 0, 255));
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

    private IterationResult rotateAndFindMaxContourArea(Mat matStarted, double angle, Scalar rcColor) {
        Point center = new Point(matStarted.width() / 2.0, matStarted.height() / 2.0);
        Mat rotateMatrix = Imgproc.getRotationMatrix2D(center, angle, 1);

        Mat dst = new Mat(matStarted.rows(), matStarted.cols(), matStarted.type());
        Imgproc.warpAffine(
                matStarted,
                dst,
                rotateMatrix,
                new org.opencv.core.Size(0, 0),
                Imgproc.INTER_LINEAR);

        Size sizeSrc = getSourceMat().size();
        IterationResult res = findMaxContourArea(angle, dst, sizeSrc.width * sizeSrc.height, logger);
        if (res.rcOut != null) {
            Imgproc.rectangle(res.mat,
                new Point(res.rcOut.x, res.rcOut.y),
                new Point(res.rcOut.x + res.rcOut.width, res.rcOut.y + res.rcOut.height),
                rcColor,
                1);
        }
        return res;
    }

    static IterationResult findMaxContourArea(double angle, Mat imageSrc, double originalMaxArea, Logger logger) {
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

        double areaLimit = originalMaxArea / 3.5;
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

        Container boxLeadToHorizont = makeBoxedCheckBox(
            () -> params.leadToHorizontal,
            v  -> params.leadToHorizontal = v,
            "",
            "Lead to horizontal",
            "params.leadToHorizontal",
            null, null);
        JCheckBox[] cbCut = { null };
        JCheckBox cbKeepSize = makeCheckBox(
                () -> params.keepSourceSize,
                v  -> params.keepSourceSize = v,
                "Keep source size",
                "params.keepSourceSize",
                null,
                () -> {
                    if (params.keepSourceSize && cbCut[0].isSelected())
                        cbCut[0].setSelected(false);
                });
        cbCut[0] = makeCheckBox(
                () -> params.cutBorders,
                v  -> params.cutBorders = v,
                "Cut borders",
                "params.cutBorders",
                null,
                () -> {
                    if (params.cutBorders && cbKeepSize.isSelected())
                        cbKeepSize.setSelected(false);
                });
        boxLeadToHorizont.add(cbKeepSize);
        boxLeadToHorizont.add(cbCut[0]);

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
