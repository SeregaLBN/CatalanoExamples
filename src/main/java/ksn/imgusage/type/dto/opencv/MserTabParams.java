package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.MserTab;
import ksn.imgusage.type.Size;

/** Init parameters for {@link MserTab} */
public class MserTabParams implements ITabParams {

    /** it compares (size_i − size_iDelta)/size_iDelta */
    public int     delta = 5;
    /** prune the area which smaller than minArea */
    public Size    minArea = new Size(6, 8);
    /** prune the area which bigger than maxArea */
    public Size    maxArea = new Size(100, 140);
    /** prune the area have similar size to its children */
    public double  maxVariation = 0.25;
    /** for color image, trace back to cut off mser with diversity less than min_diversity */
    public double  minDiversity = .2;
    /** for color image, the evolution steps */
    public int     maxEvolution = 200;
    /** for color image, the area threshold to cause re-initialize */
    public double  areaThreshold = 1.01;
    /** for color image, ignore too small margin */
    public double  minMargin = 0.003;
    /** for color image, the aperture size for edge blur */
    public int     edgeBlurSize = 5;


    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ delta=%d, minArea=%s, maxArea=%s, maxVariation=%.2f, minDiversity=%.2f, maxEvolution=%d, areaThreshold=%.2f, minMargin=%.2f, edgeBlurSize=%d }",
            delta,
            minArea,
            maxArea,
            maxVariation,
            minDiversity,
            maxEvolution,
            areaThreshold,
            minMargin,
            edgeBlurSize);
    }

    public static int onlyOdd(int value, int prevValue) {
        return CannyTabParams.onlyOdd(value, prevValue);
    }

}