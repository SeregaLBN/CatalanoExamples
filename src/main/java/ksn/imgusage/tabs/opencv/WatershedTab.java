package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.opencv.WatershedTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#ga3267243e4d3f95165d55a618c65ac6e1'>Performs a marker-based image segmentation using the watershed algorithm</a> */
public class WatershedTab extends OpencvFilterTab<WatershedTabParams> {

    public static final String TAB_TITLE = "Watershed";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Performs a marker-based image segmentation using the watershed algorithm";

    private WatershedTabParams params;

    @Override
    public Component makeTab(WatershedTabParams params) {
        this.params = params;
        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }

    @Override
    protected void applyOpencvFilter() {
        // cast to gray image
        Mat tmp = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            tmp, // src
            contours, // out
            hierarchy, // dst
            Imgproc.RETR_CCOMP,
            Imgproc.CHAIN_APPROX_SIMPLE);

        int compCount = contours.size();
        Random rnd = ThreadLocalRandom.current();
        int[][] colorTab = new int[compCount][];
        for (int i = 0; i < compCount; ++i) {
            int b = rnd.nextInt(255);
            int g = rnd.nextInt(255);
            int r = rnd.nextInt(255);
            colorTab[i] = new int[] { r, g, b };
        }

        imageMat = OpenCvHelper.to3Channel(imageMat);
        Mat markers = new Mat(imageMat.size(), CvType.CV_32S);
        Imgproc.watershed(
            imageMat, // src
            markers);

        Mat wshed = new Mat(markers.size(), CvType.CV_8UC3);
        // paint the watershed image
        for (int i=0; i < markers.rows(); ++i) {
            for (int j=0; j < markers.cols(); ++j) {
                int[] data = { 0 };
                int offset = markers.get(i, j, data);
                int index = data[0];
                if (index == -1)
                    wshed.put(i, j, new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF });
                else if (index <= 0 || index > compCount)
                    wshed.put(i, j, new byte[] { 0,0,0 });
                else
                    wshed.put(i, j, colorTab[index - 1]);
            }
        }
//        wshed = wshed*0.5 + imgGray*0.5;


        imageMat = wshed;

    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        // none..

        return box4Options;
    }

    @Override
    public WatershedTabParams getParams() {
        return params;
    }

}
