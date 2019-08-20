package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.opencv.WatershedTabParams;
import ksn.imgusage.type.dto.opencv.WatershedTabParams.EShowSteps;
import ksn.imgusage.type.opencv.CvLineType;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#ga3267243e4d3f95165d55a618c65ac6e1'>Performs a marker-based image segmentation using the watershed algorithm</a> */
public class WatershedTab extends OpencvFilterTab<WatershedTabParams> {

    public static final String TAB_TITLE = "Watershed";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Performs a marker-based image segmentation using the watershed algorithm";

    private WatershedTabParams params;

    @Override
    public Component makeTab(WatershedTabParams params) {
        if (params == null)
            params = new WatershedTabParams();
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
        // cast to gray image
        Mat imageGray = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageGray, // src
            contours , // out1
            hierarchy, // out2
            Imgproc.RETR_CCOMP,
            Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            logger.warn("No any contours found!");
            return;
        }

        final int compCount = contours.size();
        Mat markers = new Mat(imageMat.size(), CvType.CV_32SC1, Scalar.all(0));
        for (int idx = 0; idx < compCount; ++idx)
            Imgproc.drawContours(
                    markers,                         // Mat image
                    contours,                        // List<MatOfPoint> contours
                    idx,                             // int contourIdx
                    Scalar.all(idx + 1),             // Scalar color
                    CvLineType.FILLED.getVal(),      // int thickness
                    CvLineType.LINE_8.getVal(),      // int lineType
                    hierarchy,                       // Mat hierarchy
                    +2147483647,                     // int maxLevel
                    new Point());                    // Point offset

        if (params.showStep == EShowSteps.STEP1_CONTOURS) {
            imageMat = markers;
            return;
        }

        ///////////////////////////////////////////////////////////////

        imageMat = OpenCvHelper.to3Channel(imageMat);
        Imgproc.watershed(
            imageMat, // Input 8-bit 3-channel image.
            markers   // Input/output 32-bit single-channel image (map) of markers. It should have the same size as image .
        );

        if (params.showStep == EShowSteps.STEP2_WATERSHED) {
            imageMat = markers;
            return;
        }

        ///////////////////////////////////////////////////////////////

        Random rnd = ThreadLocalRandom.current();
        byte[][] colors = new byte[compCount][];
        for (int i = 0; i < compCount; ++i) {
            byte[] rgb = new byte[3];
            rnd.nextBytes(rgb);
            colors[i] = rgb;
        }

        Mat wshed = new Mat(markers.size(), CvType.CV_8UC3);
        byte[] clrWhite = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF };
        byte[] clrBlack = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00 };
        // paint the watershed image
        for (int i = 0; i < markers.rows(); i++) {
            for (int j=0; j < markers.cols(); ++j) {
                int[] data = { 0 };
                int readed = markers.get(i, j, data);
                if (readed < 1) {
                    logger.error("hmmm...");
                    continue;
                }
                int index = data[0];
                if (index == -1)
                    wshed.put(i, j, clrWhite);
                else if (index <= 0 || index > compCount)
                    wshed.put(i, j, clrBlack);
                else
                    wshed.put(i, j, colors[index - 1]);
            }
        }

        if (params.showStep == EShowSteps.STEP3_COLORIZED) {
            imageMat = wshed;
            return;
        }

        ///////////////////////////////////////////////////////////////

        // wshed = wshed*0.5 + imgGray*0.5;
        Mat dst = new Mat();
        Core.addWeighted(wshed, 0.5, OpenCvHelper.to3Channel(imageGray), 0.5, 0, dst);
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Steps = Box.createHorizontalBox();
        box4Steps.setBorder(BorderFactory.createTitledBorder("Visualization steps"));
        Box box4Steps1 = Box.createVerticalBox();
        ButtonGroup radioGroup = new ButtonGroup();
        Stream.of(EShowSteps.values())
            .forEach(step ->
        {
            JRadioButton radioBtnAlg = new JRadioButton(step.name(), params.showStep == step);
            radioBtnAlg.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    params.showStep = step;
                    logger.trace("params.showStep type changed to {}", step);
                    resetImage();
                }
            });
            box4Steps1.add(radioBtnAlg);
            radioGroup.add(radioBtnAlg);
        });
        box4Steps.add(Box.createHorizontalGlue());
        box4Steps.add(box4Steps1);
        box4Steps.add(Box.createHorizontalGlue());
        return box4Steps;
    }

    @Override
    public WatershedTabParams getParams() {
        return params;
    }

}
