package ksn.imgusage.utils;

import java.util.Arrays;
import java.util.List;

import ksn.imgusage.tabs.catalano.*;
import ksn.imgusage.tabs.opencv.*;

public final class MapFilterToTab {
    private MapFilterToTab() {}

    public static class FilterTabs {
        public final String filterName;
        public final String description;
        public FilterTabs(String filterName, String description) {
            this.filterName  = filterName;
            this.description = description;
        }
    }

    public static List<FilterTabs> getAllCatalanoTabsDescr() {
        return Arrays.asList( // alphabetical sort
            new FilterTabs(     AdaptiveContrastTab.TAB_NAME,
                                AdaptiveContrastTab.TAB_DESCRIPTION),
            new FilterTabs(     ArtifactsRemovalTab.TAB_NAME,
                                ArtifactsRemovalTab.TAB_DESCRIPTION),
            new FilterTabs(     BernsenThresholdTab.TAB_NAME,
                                BernsenThresholdTab.TAB_DESCRIPTION),
            new FilterTabs(                 BlurTab.TAB_NAME,
                                            BlurTab.TAB_DESCRIPTION),
            new FilterTabs(BradleyLocalThresholdTab.TAB_NAME,
                           BradleyLocalThresholdTab.TAB_DESCRIPTION),
            new FilterTabs( BrightnessCorrectionTab.TAB_NAME,
                            BrightnessCorrectionTab.TAB_DESCRIPTION),
            new FilterTabs(      FrequencyFilterTab.TAB_NAME,
                                 FrequencyFilterTab.TAB_DESCRIPTION),
            new FilterTabs(               RotateTab.TAB_NAME,
                                          RotateTab.TAB_DESCRIPTION)
        );
    }

    public static List<FilterTabs> getAllOpencvTabsDescr() {
        return Arrays.<FilterTabs>asList( // alphabetical sort
            new FilterTabs(        AsIsTab.TAB_NAME,
                                   AsIsTab.TAB_DESCRIPTION),
            new FilterTabs(       CannyTab.TAB_NAME,
                                  CannyTab.TAB_DESCRIPTION),
            new FilterTabs(FindContoursTab.TAB_NAME,
                           FindContoursTab.TAB_DESCRIPTION),
            new FilterTabs(GaussianBlurTab.TAB_NAME,
                           GaussianBlurTab.TAB_DESCRIPTION),
            new FilterTabs(MorphologyExTab.TAB_NAME,
                           MorphologyExTab.TAB_DESCRIPTION),
            new FilterTabs(   ThresholdTab.TAB_NAME,
                              ThresholdTab.TAB_DESCRIPTION)
        );
    }


    /** map OpenCV filters to tab classes */
    public static Class<? extends OpencvFilterTab> getOpencvTabClass(String fullFilterTabName) {
        if (fullFilterTabName.startsWith(OpencvFilterTab.TAB_PREFIX))
            return null;

        String  filterTabName = fullFilterTabName.substring(OpencvFilterTab.TAB_PREFIX.length());
        switch (filterTabName) { // alphabetical sort
        case               AsIsTab.TAB_NAME:
            return         AsIsTab.class;
        case              CannyTab.TAB_NAME:
            return        CannyTab.class;
        case       FindContoursTab.TAB_NAME:
            return FindContoursTab.class;
        case       GaussianBlurTab.TAB_NAME:
            return GaussianBlurTab.class;
        case       MorphologyExTab.TAB_NAME:
            return MorphologyExTab.class;
        case          ThresholdTab.TAB_NAME:
            return    ThresholdTab.class;
        default:
            return null;
        }
    }

    /** map Catalano filters to tab classes */
    public static Class<? extends CatalanoFilterTab> getCatalanoTabClass(String fullFilterTabName) {
        if (fullFilterTabName.startsWith(CatalanoFilterTab.TAB_PREFIX))
            return null;

        String  filterTabName = fullFilterTabName.substring(CatalanoFilterTab.TAB_PREFIX.length());
        // alphabetical sort
        if (filterTabName.equals(     AdaptiveContrastTab.TAB_NAME))
            return                    AdaptiveContrastTab.class;
        if (filterTabName.equals(     ArtifactsRemovalTab.TAB_NAME))
            return                    ArtifactsRemovalTab.class;
        if (filterTabName.equals(     BernsenThresholdTab.TAB_NAME))
            return                    BernsenThresholdTab.class;
        if (filterTabName.equals(                 BlurTab.TAB_NAME))
            return                                BlurTab.class;
        if (filterTabName.equals(BradleyLocalThresholdTab.TAB_NAME))
            return               BradleyLocalThresholdTab.class;
        if (filterTabName.equals( BrightnessCorrectionTab.TAB_NAME))
            return                BrightnessCorrectionTab.class;
        if (filterTabName.equals(      FrequencyFilterTab.TAB_NAME))
            return                     FrequencyFilterTab.class;
        if (filterTabName.equals(               RotateTab.TAB_NAME))
            return                              RotateTab.class;
        return null;
    }

}
