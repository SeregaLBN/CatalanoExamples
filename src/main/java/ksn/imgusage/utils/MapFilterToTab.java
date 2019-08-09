package ksn.imgusage.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ksn.imgusage.tabs.ITab;
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

    public static List<FilterTabs> getAllCatalanoTabs() {
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

    public static List<FilterTabs> getAllOpencvTabs() {
        return Arrays.<FilterTabs>asList( // alphabetical sort
            new FilterTabs(        AsIsTab.TAB_NAME,
                                   AsIsTab.TAB_DESCRIPTION),
            new FilterTabs(       CannyTab.TAB_NAME,
                                  CannyTab.TAB_DESCRIPTION),
            new FilterTabs(GaussianBlurTab.TAB_NAME,
                           GaussianBlurTab.TAB_DESCRIPTION),
            new FilterTabs(MorphologyExTab.TAB_NAME,
                           MorphologyExTab.TAB_DESCRIPTION),
            new FilterTabs(   ThresholdTab.TAB_NAME,
                              ThresholdTab.TAB_DESCRIPTION)
        );
    }


    /** map OpenCV filters to tab classes */
    public static Stream<Supplier<ITab>> getOpencvMapping(BiFunction<String /* filterName */, Class<? extends ITab>, ITab> opencvHandler) {
        return Stream.of( // alphabetical sort
            () -> opencvHandler.apply(        AsIsTab.TAB_NAME,
                                              AsIsTab.class),
            () -> opencvHandler.apply(       CannyTab.TAB_NAME,
                                             CannyTab.class),
            () -> opencvHandler.apply(GaussianBlurTab.TAB_NAME,
                                      GaussianBlurTab.class),
            () -> opencvHandler.apply(MorphologyExTab.TAB_NAME,
                                      MorphologyExTab.class),
            () -> opencvHandler.apply(   ThresholdTab.TAB_NAME,
                                         ThresholdTab.class)
        );
    }

    /** map Catalano filters to tab classes */
    public static Stream<Supplier<ITab>> getCatalanoMapping(BiFunction<String /* Catalano filter name */, Class<? extends ITab>, ITab> catalanoHandler) {
        return Stream.of( // alphabetical sort
            () -> catalanoHandler.apply(     AdaptiveContrastTab.TAB_NAME,
                                             AdaptiveContrastTab.class),
            () -> catalanoHandler.apply(     ArtifactsRemovalTab.TAB_NAME,
                                             ArtifactsRemovalTab.class),
            () -> catalanoHandler.apply(     BernsenThresholdTab.TAB_NAME,
                                             BernsenThresholdTab.class),
            () -> catalanoHandler.apply(                 BlurTab.TAB_NAME,
                                                         BlurTab.class),
            () -> catalanoHandler.apply(BradleyLocalThresholdTab.TAB_NAME,
                                        BradleyLocalThresholdTab.class),
            () -> catalanoHandler.apply( BrightnessCorrectionTab.TAB_NAME,
                                         BrightnessCorrectionTab.class),
            () -> catalanoHandler.apply(      FrequencyFilterTab.TAB_NAME,
                                              FrequencyFilterTab.class),
            () -> catalanoHandler.apply(               RotateTab.TAB_NAME,
                                                       RotateTab.class)
        );
    }

}
