package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.AddWeightedTab;

/** Init parameters for {@link AddWeightedTab} */
public class AddWeightedTabParams implements ITabParams {

    public double  alpha = 1.0;
    public double  beta  = 0;
    public double  gamma = 0;
    public int     dtype = -1;

    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ alpha=%.2f, beta=%.2f, gamma=%.2f, dtype=%d }",
            alpha,
            beta,
            gamma,
            dtype);
    }

}
