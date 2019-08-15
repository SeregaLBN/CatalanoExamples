package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.BradleyLocalThresholdTab;

/** Init parameters for {@link BradleyLocalThresholdTab} */
public class BradleyLocalThresholdTabParams implements ITabParams {

    public int    windowSize;
    public double pixelBrightnessDiff;

    public BradleyLocalThresholdTabParams() {}

    public BradleyLocalThresholdTabParams(
        int    windowSize,
        double pixelBrightnessDiff)
    {
        this.windowSize          = windowSize;
        this.pixelBrightnessDiff = pixelBrightnessDiff;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ windowSize=%d, pixelBrightnessDiff=%.2f }",
            windowSize,
            pixelBrightnessDiff);
    }

}
