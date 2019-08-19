package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.AdaptiveContrastTab;

/** Init parameters for {@link AdaptiveContrastTab} */
public class AdaptiveContrastTabParams implements ITabParams {

    public int    winSize = 20;
    public double k1      = 0.3;
    public double k2      = 0.6;
    public double minGain = 0.1;
    public double maxGain = 1;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ winSize=%d, k1=%.2f, k2=%.2f, minGain=%.2f, maxGain=%.2f }",
            winSize,
            k1,
            k1,
            minGain,
            maxGain);
    }

}
