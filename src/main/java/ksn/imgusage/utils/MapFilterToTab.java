package ksn.imgusage.utils;

import java.util.Arrays;
import java.util.List;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabParams;
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


    /** map filters to tab classes */
    public static Class<? extends ITab<?>> getTabClass(String filterTabFullName) {
        switch (filterTabFullName) {

        case       FirstTab.TAB_FULL_NAME:
            return FirstTab.class;

        // OpenCV
        // alphabetical sort
        case               AsIsTab.TAB_FULL_NAME:
            return         AsIsTab.class;
        case              CannyTab.TAB_FULL_NAME:
            return        CannyTab.class;
        case       FindContoursTab.TAB_FULL_NAME:
            return FindContoursTab.class;
        case       GaussianBlurTab.TAB_FULL_NAME:
            return GaussianBlurTab.class;
        case       MorphologyExTab.TAB_FULL_NAME:
            return MorphologyExTab.class;
        case          ThresholdTab.TAB_FULL_NAME:
            return    ThresholdTab.class;
        default:

            // Catalano-Framework
            // alphabetical sort
            if (filterTabFullName.equals(     AdaptiveContrastTab.TAB_FULL_NAME))
                return                        AdaptiveContrastTab.class;
            if (filterTabFullName.equals(     ArtifactsRemovalTab.TAB_FULL_NAME))
                return                        ArtifactsRemovalTab.class;
            if (filterTabFullName.equals(     BernsenThresholdTab.TAB_FULL_NAME))
                return                        BernsenThresholdTab.class;
            if (filterTabFullName.equals(                 BlurTab.TAB_FULL_NAME))
                return                                    BlurTab.class;
            if (filterTabFullName.equals(BradleyLocalThresholdTab.TAB_FULL_NAME))
                return                   BradleyLocalThresholdTab.class;
            if (filterTabFullName.equals( BrightnessCorrectionTab.TAB_FULL_NAME))
                return                    BrightnessCorrectionTab.class;
            if (filterTabFullName.equals(      FrequencyFilterTab.TAB_FULL_NAME))
                return                         FrequencyFilterTab.class;
            if (filterTabFullName.equals(               RotateTab.TAB_FULL_NAME))
                return                                  RotateTab.class;

            return null;
        }
    }

    /** map filters to tab params classes */
    public static Class<? extends ITabParams> getTabParamsClass(String filterTabFullName) {
        switch (filterTabFullName) {

        case       FirstTab.TAB_FULL_NAME:
            return FirstTab.Params.class;

        // OpenCV
        // alphabetical sort
        case               AsIsTab.TAB_FULL_NAME:
            return         AsIsTab.Params.class;
        case              CannyTab.TAB_FULL_NAME:
            return        CannyTab.Params.class;
        case       FindContoursTab.TAB_FULL_NAME:
            return FindContoursTab.Params.class;
        case       GaussianBlurTab.TAB_FULL_NAME:
            return GaussianBlurTab.Params.class;
        case       MorphologyExTab.TAB_FULL_NAME:
            return MorphologyExTab.Params.class;
        case          ThresholdTab.TAB_FULL_NAME:
            return    ThresholdTab.Params.class;
        default:

            // Catalano-Framework
            // alphabetical sort
            if (filterTabFullName.equals(     AdaptiveContrastTab.TAB_FULL_NAME))
                return                        AdaptiveContrastTab.Params.class;
            if (filterTabFullName.equals(     ArtifactsRemovalTab.TAB_FULL_NAME))
                return                        ArtifactsRemovalTab.Params.class;
            if (filterTabFullName.equals(     BernsenThresholdTab.TAB_FULL_NAME))
                return                        BernsenThresholdTab.Params.class;
            if (filterTabFullName.equals(                 BlurTab.TAB_FULL_NAME))
                return                                    BlurTab.Params.class;
            if (filterTabFullName.equals(BradleyLocalThresholdTab.TAB_FULL_NAME))
                return                   BradleyLocalThresholdTab.Params.class;
            if (filterTabFullName.equals( BrightnessCorrectionTab.TAB_FULL_NAME))
                return                    BrightnessCorrectionTab.Params.class;
            if (filterTabFullName.equals(      FrequencyFilterTab.TAB_FULL_NAME))
                return                         FrequencyFilterTab.Params.class;
            if (filterTabFullName.equals(               RotateTab.TAB_FULL_NAME))
                return                                  RotateTab.Params.class;

            return null;
        }
    }

}
