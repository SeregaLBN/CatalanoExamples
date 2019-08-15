package ksn.imgusage.type.dto.catalano;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.BrightnessCorrectionTab;

/** Init parameters for {@link BrightnessCorrectionTab} */
public class BrightnessCorrectionTabParams implements ITabParams {

    public int adjust;

    public BrightnessCorrectionTabParams() {}

    public BrightnessCorrectionTabParams(int adjustValue) { this.adjust = adjustValue; }

    @Override
    public String toString() { return "{ adjust=" + adjust + " }"; }

}
