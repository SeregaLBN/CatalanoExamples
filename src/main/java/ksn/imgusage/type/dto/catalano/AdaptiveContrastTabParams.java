package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.AdaptiveContrastTab;

/** Init parameters for {@link AdaptiveContrastTab} */
public class AdaptiveContrastTabParams implements ITabParams {

    public int    winSize;
    public double k1;
    public double k2;
    public double minGain;
    public double maxGain;

    public AdaptiveContrastTabParams() {}

    public AdaptiveContrastTabParams(
        int    winSize,
        double k1,
        double k2,
        double minGain,
        double maxGain)
    {
        this.winSize = winSize;
        this.k1      = k1;
        this.k2      = k2;
        this.minGain = minGain;
        this.maxGain = maxGain;
    }

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
