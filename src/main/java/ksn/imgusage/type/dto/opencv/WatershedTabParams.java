package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.WatershedTab;

/** Init parameters for {@link WatershedTab} */
public class WatershedTabParams implements ITabParams {

    public enum EShowSteps {
        STEP1_CONTOURS,
        STEP2_WATERSHED,
        STEP3_COLORIZED,
        STEP4_COMBINE_TO_ORIGINAL
    }

    public EShowSteps showStep = EShowSteps.STEP4_COMBINE_TO_ORIGINAL;

    @Override
    public String toString() {
        return "{ showStep=" + showStep + " }";
    }

}
