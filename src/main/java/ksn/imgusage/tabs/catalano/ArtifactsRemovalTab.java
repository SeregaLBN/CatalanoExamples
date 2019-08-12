package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;

import Catalano.Imaging.Filters.ArtifactsRemoval;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/ArtifactsRemoval.java'>Remove artifacts caused by uneven lightning</a> */
public class ArtifactsRemovalTab extends CatalanoFilterTab {

    public static final String TAB_NAME = ArtifactsRemoval.class.getSimpleName();
    public static final String TAB_DESCRIPTION = "Remove artifacts caused by uneven lightning";

    private static final int MIN_WINDOW_SIZE = 1;
    private static final int MAX_WINDOW_SIZE = 201;

    private final SliderIntModel modelWinSize;

    public ArtifactsRemovalTab(ITabHandler tabHandler, ITab source) {
        this(tabHandler, source, true, 15);
    }

    public ArtifactsRemovalTab(ITabHandler tabHandler, ITab source, boolean boosting, int windowSize) {
        super(tabHandler, source, boosting, true);
        this.modelWinSize = new SliderIntModel(windowSize, 0, MIN_WINDOW_SIZE, MAX_WINDOW_SIZE);

        makeTab();
    }

    @Override
    public String getTabName() { return TAB_NAME; }

    @Override
    protected void applyCatalanoFilter() {
        new ArtifactsRemoval(modelWinSize.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(Box box4Options) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder(getTabName() + " options"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWinSize, "WinSize", "Size of window"));
        boxOptions.add(Box.createHorizontalGlue());

        box4Options.add(boxOptions);

        modelWinSize.getWrapped().addChangeListener(ev -> {
            logger.trace("modelWinSize: value={}", modelWinSize.getFormatedText());
            resetImage();
        });
    }

    @Override
    public void printParams() {
        logger.info("windowSize={}", modelWinSize.getFormatedText());
    }

}
