package ksn.imgusage.type.dto.opencv;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.AsIsTab;

/** Init parameters for {@link AsIsTab} */
public class AsIsTabParams implements ITabParams {

    public boolean useGray;

    public AsIsTabParams() {}

    public AsIsTabParams(boolean useGray) { this.useGray = useGray; }

    @Override
    public String toString() { return "{ useGray=" + useGray + " }"; }

}
