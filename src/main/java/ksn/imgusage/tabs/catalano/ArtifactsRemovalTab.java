package ksn.imgusage.tabs.catalano;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.ArtifactsRemoval;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/ArtifactsRemoval.java'>Remove artifacts caused by uneven lightning</a> */
public class ArtifactsRemovalTab extends CatalanoFilterTab<ArtifactsRemovalTab.Params> {

    public static final String TAB_NAME = ArtifactsRemoval.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Remove artifacts caused by uneven lightning";

    private static final int MIN_WINDOW_SIZE = 1;
    private static final int MAX_WINDOW_SIZE = 201;

    public static class Params implements ITabParams {
        public int windowSize;
        public Params(int windowSize) { this.windowSize = windowSize; }
        @Override
        public String toString() {
            return "{ windowSize=" + windowSize + " }";
        }
    }

    private final Params params;

    public ArtifactsRemovalTab(ITabHandler tabHandler, ITab<?> source) {
        this(tabHandler, source, new Params(15));
    }

    public ArtifactsRemovalTab(ITabHandler tabHandler, ITab<?> source, Params params) {
        super(tabHandler, source, true);
        this.params = params;

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new ArtifactsRemoval(params.windowSize)
            .applyInPlace(imageFBmp);
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderIntModel modelWinSize = new SliderIntModel(params.windowSize, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);

        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWinSize, "WinSize", "Size of window"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelWinSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelWinSize: value={}", modelWinSize.getFormatedText());
            params.windowSize = modelWinSize.getValue();
            resetImage();
        });

        return box4Options;
    }

    @Override
    public Params getParams() {
        return params;
    }

}
