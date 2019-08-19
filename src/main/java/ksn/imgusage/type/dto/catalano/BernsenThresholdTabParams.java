package ksn.imgusage.type.dto.catalano;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.BernsenThresholdTab;

/** Init parameters for {@link BernsenThresholdTab} */
public class BernsenThresholdTabParams implements ITabParams {

    public int    radius            = 15;
    public double contrastThreshold = 15;

    @Override
    public String toString() {
        return String.format(Locale.US, "{ radius=%d, contrastThreshold=%.2f }", radius, contrastThreshold);
    }

}
