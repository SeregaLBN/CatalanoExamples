package ksn.imgusage.utils;

import java.util.Arrays;
import java.util.List;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.catalano.*;
import ksn.imgusage.tabs.catalano.BlurTab;
import ksn.imgusage.tabs.opencv.*;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.type.dto.catalano.*;
import ksn.imgusage.type.dto.catalano.BlurTabParams;
import ksn.imgusage.type.dto.opencv.*;

public final class MapperFilter {
    private MapperFilter() {}

    /** pair-tuple */
    public static class FilterTabs {
        public final String filterTitle;
        public final String description;
        public FilterTabs(String filterTitle, String description) {
            this.filterTitle  = filterTitle;
            this.description = description;
        }
    }

    public static List<FilterTabs> getAllCatalanoTabsDescr() {
        return Arrays.asList( // alphabetical sort
            new FilterTabs(     AdaptiveContrastTab.TAB_TITLE,
                                AdaptiveContrastTab.TAB_DESCRIPTION),
            new FilterTabs(     ArtifactsRemovalTab.TAB_TITLE,
                                ArtifactsRemovalTab.TAB_DESCRIPTION),
            new FilterTabs(     BernsenThresholdTab.TAB_TITLE,
                                BernsenThresholdTab.TAB_DESCRIPTION),
            new FilterTabs(                 BlurTab.TAB_TITLE,
                                            BlurTab.TAB_DESCRIPTION),
            new FilterTabs(BradleyLocalThresholdTab.TAB_TITLE,
                           BradleyLocalThresholdTab.TAB_DESCRIPTION),
            new FilterTabs( BrightnessCorrectionTab.TAB_TITLE,
                            BrightnessCorrectionTab.TAB_DESCRIPTION),
            new FilterTabs(      FrequencyFilterTab.TAB_TITLE,
                                 FrequencyFilterTab.TAB_DESCRIPTION),
            new FilterTabs(               RotateTab.TAB_TITLE,
                                          RotateTab.TAB_DESCRIPTION)
        );
    }

    public static List<FilterTabs> getAllOpencvTabsDescr() {
        return Arrays.<FilterTabs>asList( // alphabetical sort
            new FilterTabs(                         AsIsTab.TAB_TITLE,
                                                    AsIsTab.TAB_DESCRIPTION),
            new FilterTabs(                    BilateralTab.TAB_TITLE,
                                               BilateralTab.TAB_DESCRIPTION),
            new FilterTabs(ksn.imgusage.tabs.opencv.BlurTab.TAB_TITLE,
                           ksn.imgusage.tabs.opencv.BlurTab.TAB_DESCRIPTION),
            new FilterTabs(                          BoxTab.TAB_TITLE,
                                                     BoxTab.TAB_DESCRIPTION),
            new FilterTabs(                        CannyTab.TAB_TITLE,
                                                   CannyTab.TAB_DESCRIPTION),
            new FilterTabs(                 FindContoursTab.TAB_TITLE,
                                            FindContoursTab.TAB_DESCRIPTION),
            new FilterTabs(                 GaussianBlurTab.TAB_TITLE,
                                            GaussianBlurTab.TAB_DESCRIPTION),
            new FilterTabs(                 MorphologyExTab.TAB_TITLE,
                                            MorphologyExTab.TAB_DESCRIPTION),
            new FilterTabs(                    ThresholdTab.TAB_TITLE,
                                               ThresholdTab.TAB_DESCRIPTION),
            new FilterTabs(                    WatershedTab.TAB_TITLE,
                                               WatershedTab.TAB_DESCRIPTION)
        );
    }


    /** map filters to tab classes */
    public static Class<? extends ITab<?>> getTabClass(String filterTabFullName) {
        switch (filterTabFullName) {

        case       FirstTab.TAB_NAME:
            return FirstTab.class;

        // OpenCV
        // alphabetical sort
        case                                AsIsTab.TAB_NAME:
            return                          AsIsTab.class;
        case                           BilateralTab.TAB_NAME:
            return                     BilateralTab.class;
        case       ksn.imgusage.tabs.opencv.BlurTab.TAB_NAME:
            return ksn.imgusage.tabs.opencv.BlurTab.class;
        case                                 BoxTab.TAB_NAME:
            return                           BoxTab.class;
        case                               CannyTab.TAB_NAME:
            return                         CannyTab.class;
        case                        FindContoursTab.TAB_NAME:
            return                  FindContoursTab.class;
        case                        GaussianBlurTab.TAB_NAME:
            return                  GaussianBlurTab.class;
        case                        MorphologyExTab.TAB_NAME:
            return                  MorphologyExTab.class;
        case                           ThresholdTab.TAB_NAME:
            return                     ThresholdTab.class;
        case                           WatershedTab.TAB_NAME:
            return                     WatershedTab.class;
        default:

            // Catalano-Framework
            // alphabetical sort
            if (filterTabFullName.equals(     AdaptiveContrastTab.TAB_NAME))
                return                        AdaptiveContrastTab.class;
            if (filterTabFullName.equals(     ArtifactsRemovalTab.TAB_NAME))
                return                        ArtifactsRemovalTab.class;
            if (filterTabFullName.equals(     BernsenThresholdTab.TAB_NAME))
                return                        BernsenThresholdTab.class;
            if (filterTabFullName.equals(                 BlurTab.TAB_NAME))
                return                                    BlurTab.class;
            if (filterTabFullName.equals(BradleyLocalThresholdTab.TAB_NAME))
                return                   BradleyLocalThresholdTab.class;
            if (filterTabFullName.equals( BrightnessCorrectionTab.TAB_NAME))
                return                    BrightnessCorrectionTab.class;
            if (filterTabFullName.equals(      FrequencyFilterTab.TAB_NAME))
                return                         FrequencyFilterTab.class;
            if (filterTabFullName.equals(               RotateTab.TAB_NAME))
                return                                  RotateTab.class;

            return null;
        }
    }

    /** map filters to tab params classes */
    public static Class<? extends ITabParams> getTabParamsClass(String filterTabFullName) {
        switch (filterTabFullName) {

        case       FirstTab.TAB_NAME:
            return FirstTabParams.class;

        // OpenCV
        // alphabetical sort
        case                                    AsIsTab.TAB_NAME:
            return                              AsIsTabParams.class;
        case                               BilateralTab.TAB_NAME:
            return                         BilateralTabParams.class;
        case       ksn.imgusage.tabs.    opencv.BlurTab.TAB_NAME:
            return ksn.imgusage.type.dto.opencv.BlurTabParams.class;
        case                                     BoxTab.TAB_NAME:
            return                               BoxTabParams.class;
        case                                   CannyTab.TAB_NAME:
            return                             CannyTabParams.class;
        case                            FindContoursTab.TAB_NAME:
            return                      FindContoursTabParams.class;
        case                            GaussianBlurTab.TAB_NAME:
            return                      GaussianBlurTabParams.class;
        case                            MorphologyExTab.TAB_NAME:
            return                      MorphologyExTabParams.class;
        case                               ThresholdTab.TAB_NAME:
            return                         ThresholdTabParams.class;
        case                               WatershedTab.TAB_NAME:
            return                         WatershedTabParams.class;
        default:

            // Catalano-Framework
            // alphabetical sort
            if (filterTabFullName.equals(     AdaptiveContrastTab.TAB_NAME))
                return                        AdaptiveContrastTabParams.class;
            if (filterTabFullName.equals(     ArtifactsRemovalTab.TAB_NAME))
                return                        ArtifactsRemovalTabParams.class;
            if (filterTabFullName.equals(     BernsenThresholdTab.TAB_NAME))
                return                        BernsenThresholdTabParams.class;
            if (filterTabFullName.equals(                 BlurTab.TAB_NAME))
                return                                    BlurTabParams.class;
            if (filterTabFullName.equals(BradleyLocalThresholdTab.TAB_NAME))
                return                   BradleyLocalThresholdTabParams.class;
            if (filterTabFullName.equals( BrightnessCorrectionTab.TAB_NAME))
                return                    BrightnessCorrectionTabParams.class;
            if (filterTabFullName.equals(      FrequencyFilterTab.TAB_NAME))
                return                         FrequencyFilterTabParams.class;
            if (filterTabFullName.equals(               RotateTab.TAB_NAME))
                return                                  RotateTabParams.class;

            return null;
        }
    }

}
