package ksn.imgusage.tabs.opencv;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.opencv.HistogramEqualizationTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d6/dc7/group__imgproc__hist.html#ga7e54091f0c937d49bf84152a16f76d6e'>Equalizes the histogram of a grayscale image</a>
 * <br>
 * <a href='https://docs.opencv.org/2.4/doc/tutorials/imgproc/histograms/histogram_equalization/histogram_equalization.html'>Histogram Equalization</a>
 */
public class HistogramEqualizationTab extends OpencvFilterTab<HistogramEqualizationTabParams> {

    public static final String TAB_TITLE = "Histogram Equalization";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Equalizes the histogram of a grayscale image";

    private HistogramEqualizationTabParams params;

    @Override
    public Component makeTab(HistogramEqualizationTabParams params) {
        if (params == null)
            params = new HistogramEqualizationTabParams();
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
        // src  Source 8-bit single channel image.
        imageMat = OpenCvHelper.toGray(imageMat);

        Mat dst = new Mat();
        Imgproc.equalizeHist(
            imageMat, // src
            dst);
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

//        box4Options.add(...);

        return box4Options;
    }

    @Override
    public HistogramEqualizationTabParams getParams() {
        return params;
    }

}
