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
    public int     minArea = 48;
    /** prune the area which bigger than maxArea */
    public int     maxArea = 14000;
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

    /** Additional restrictions on the minimum symbol size */
    public Size    minSymbol = new Size(1, 1);
    /** Additional restrictions on the maximum symbol size */
    public Size    maxSymbol = new Size(500, 700);

    /** Show mask regions - MSER result */
    public boolean showRegions = true;
    /** Show inner contours */
    public boolean showInner = true;
    /** Invert regions */
    public boolean invert;

    /** Show rectangle of regions (mark single char) */
    public boolean markChars;
    /** Show groups of regions as a word (mark word) */
    public boolean markWords;
    /** Show word groups as a line */
    public boolean markLines;


    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ delta=%d, minArea=%s, maxArea=%s, maxVariation=%.2f, minDiversity=%.2f, maxEvolution=%d, areaThreshold=%.2f, minMargin=%.2f, edgeBlurSize=%d"
            + ", minSymbol=%s, maxSymbol=%s, "
            + ", showRegions=%b, invert=%b, showInner=%b, markChars=%b, markWords=%b, markLines=%b }",
            delta,
            minArea,
            maxArea,
            maxVariation,
            minDiversity,
            maxEvolution,
            areaThreshold,
            minMargin,
            edgeBlurSize,
            minSymbol,
            maxSymbol,
            showRegions,
            invert,
            showInner,
            markChars,
            markWords,
            markLines);
    }

    public static int onlyOdd(int value, int prevValue) {
        return CannyTabParams.onlyOdd(value, prevValue);
    }

}
