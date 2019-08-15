package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.BernsenThresholdTab;

/** Init parameters for {@link BernsenThresholdTab} */
public class BernsenThresholdTabParams implements ITabParams {

    public int    radius;
    public double contrastThreshold;

    public BernsenThresholdTabParams() {}

    public BernsenThresholdTabParams(int radius, double contrastThreshold) {
        this.radius = radius;
        this.contrastThreshold = contrastThreshold;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "{ radius=%d, contrastThreshold=%.2f }", radius, contrastThreshold);
    }

}
