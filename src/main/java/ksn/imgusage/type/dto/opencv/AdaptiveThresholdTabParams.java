package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.AdaptiveThresholdTab;
import ksn.imgusage.type.opencv.CvAdaptiveThresholdTypes;
import ksn.imgusage.type.opencv.CvBorderTypes;
import ksn.imgusage.type.opencv.CvThresholdTypes;

/** Init parameters for {@link AdaptiveThresholdTab} */
public class AdaptiveThresholdTabParams implements ITabParams {

    /** Non-zero value assigned to the pixels for which the condition is satisfied  */
    public double maxVal = 250;

    /** Adaptive thresholding algorithm to use, see {@link CvAdaptiveThresholdTypes}.
     * The {@link CvBorderTypes#BORDER_REPLICATE} | {@link CvBorderTypes#BORDER_ISOLATED} is used to process boundaries.  */
    public CvAdaptiveThresholdTypes adaptiveMethod = CvAdaptiveThresholdTypes.ADAPTIVE_THRESH_MEAN_C;

    /** Thresholding type that must be either {@link CvThresholdTypes#THRESH_BINARY} or {@link CvThresholdTypes#THRESH_BINARY_INV}.  */
    private CvThresholdTypes threshType = CvThresholdTypes.THRESH_BINARY;

    /** Size of a pixel neighborhood that is used to calculate a threshold value for the pixel: 3, 5, 7, and so on. */
    public int blockSize = 3;

    /** Constant subtracted from the mean or weighted mean (see the details below). Normally, it is positive but may be zero or negative as well */
    public double c = 0;

    public CvThresholdTypes getThreshType() {
        return threshType;
    }
    public void setThreshType(CvThresholdTypes threshType) {
        if ((threshType == CvThresholdTypes.THRESH_BINARY) || (threshType == CvThresholdTypes.THRESH_BINARY_INV))
            this.threshType = threshType;
        else
            throw new IllegalArgumentException("Unsupported threshType=" + threshType);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ maxVal=%.2f, adaptiveMethod=%s, threshType=%s, blockSize=%d, c=%.2f }",
            maxVal,
            adaptiveMethod==null ? "null" : adaptiveMethod.name(),
            threshType    ==null ? "null" : threshType.name(),
            blockSize,
            c);
    }

}
