package ksn.imgusage.tabs.opencv.custom;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.OpencvFilterTab;

public abstract class AnotherTab<TTabParams extends ITabParams> extends OpencvFilterTab<TTabParams> {

    public static final String TAB_PREFIX = "OpenCV:Custom:";

}
