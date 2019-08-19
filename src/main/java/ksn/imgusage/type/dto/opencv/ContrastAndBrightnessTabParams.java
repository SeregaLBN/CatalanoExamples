package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.ContrastAndBrightnessTab;

/** Init parameters for {@link ContrastAndBrightnessTab} */
public class ContrastAndBrightnessTabParams implements ITabParams {

    public double  alpha = 1.0;
    public int     beta  = 0;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ alpha=%.2f, beta=%d }",
            alpha,
            beta);
    }

}
