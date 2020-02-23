package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.ColorizedTab;

/** Init parameters for {@link ColorizedTab} */
public class ColorizedTabParams implements ITabParams {

    public enum ECastTo {
        GRAY,
        RGB;

        public String userText() {
            switch (this) {
            case GRAY: return "Gray";
            case RGB : return "RGB";
            default:
                return name();
            }
        }

        public String userTip() {
            switch (this) {
            case GRAY: return "Cast image to gray colors";
            case RGB : return "Cast image to full colors";
            default:
                return name();
            }
        }
    }

    /** Transform all image colors to... */
    public ECastTo colorsTo = ECastTo.GRAY;

    /** Applies white balancing to the input image */
    public boolean useWhiteBalancer;


    @Override
    public String toString() {
        return "{ colorsTo=" + colorsTo
            + ", useWhiteBalancer=" + useWhiteBalancer
            + " }"; }

}
