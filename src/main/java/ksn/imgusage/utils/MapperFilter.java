package ksn.imgusage.utils;

import java.util.Arrays;
import java.util.List;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.*;
import ksn.imgusage.tabs.opencv.*;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.type.dto.catalano.*;
import ksn.imgusage.type.dto.opencv.*;

public final class MapperFilter {
    private MapperFilter() {}

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
            return FirstTabParams.class;

        // OpenCV
        // alphabetical sort
        case               AsIsTab.TAB_FULL_NAME:
            return         AsIsTabParams.class;
        case              CannyTab.TAB_FULL_NAME:
            return        CannyTabParams.class;
        case       FindContoursTab.TAB_FULL_NAME:
            return FindContoursTabParams.class;
        case       GaussianBlurTab.TAB_FULL_NAME:
            return GaussianBlurTabParams.class;
        case       MorphologyExTab.TAB_FULL_NAME:
            return MorphologyExTabParams.class;
        case          ThresholdTab.TAB_FULL_NAME:
            return    ThresholdTabParams.class;
        default:

            // Catalano-Framework
            // alphabetical sort
            if (filterTabFullName.equals(     AdaptiveContrastTab.TAB_FULL_NAME))
                return                        AdaptiveContrastTabParams.class;
            if (filterTabFullName.equals(     ArtifactsRemovalTab.TAB_FULL_NAME))
                return                        ArtifactsRemovalTabParams.class;
            if (filterTabFullName.equals(     BernsenThresholdTab.TAB_FULL_NAME))
                return                        BernsenThresholdTabParams.class;
            if (filterTabFullName.equals(                 BlurTab.TAB_FULL_NAME))
                return                                    BlurTabParams.class;
            if (filterTabFullName.equals(BradleyLocalThresholdTab.TAB_FULL_NAME))
                return                   BradleyLocalThresholdTabParams.class;
            if (filterTabFullName.equals( BrightnessCorrectionTab.TAB_FULL_NAME))
                return                    BrightnessCorrectionTabParams.class;
            if (filterTabFullName.equals(      FrequencyFilterTab.TAB_FULL_NAME))
                return                         FrequencyFilterTabParams.class;
            if (filterTabFullName.equals(               RotateTab.TAB_FULL_NAME))
                return                                  RotateTabParams.class;

            return null;
        }
    }

}
