package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.BradleyLocalThresholdTab;

/** Init parameters for {@link BradleyLocalThresholdTab} */
public class BradleyLocalThresholdTabParams implements ITabParams {

    public int    windowSize          = 41;
    public double pixelBrightnessDiff = 0.15;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ windowSize=%d, pixelBrightnessDiff=%.2f }",
            windowSize,
            pixelBrightnessDiff);
    }

}
