package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.ContrastAndBrightnessTab;

/** Init parameters for {@link ContrastAndBrightnessTab} */
public class ContrastAndBrightnessTabParams implements ITabParams {

    public double alpha = 1.0;
    public double beta  = 0;

    /** use automatic optimization of brightness and contrast through clipping a histogram */
    public boolean autoClipHist = false;
    /** histogram clipping in percent 1..99 */
    public int clipHistPercent = 25;

    /** use automatic white background adjustment */
    public boolean autoWhiteBkAjust = false;
    /** white percentage 1..99 */
    public int whiteBkPercent = 65;


    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ alpha=%.2f"
            + ", beta=%.2f"
            + ", autoClipHist=%b"
            + ", clipHistPercent=%d"
            + ", autoWhiteBkAjust=%b"
            + ", whiteBkPercent=%d"
            + " }",
            alpha,
            beta,
            autoClipHist,
            clipHistPercent,
            autoWhiteBkAjust,
            whiteBkPercent);
    }

}
