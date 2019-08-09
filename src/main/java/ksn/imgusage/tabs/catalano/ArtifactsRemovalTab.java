package ksn.imgusage.tabs.catalano;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import Catalano.Imaging.Filters.ArtifactsRemoval;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

/** <a href='https://github.com/DiegoCatalano/Catalano-Framework/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/ArtifactsRemoval.java'>Remove artifacts caused by uneven lightning</a> */
public class ArtifactsRemovalTab extends CatalanoFilterTab {

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
    public String getTabName() { return ArtifactsRemoval.class.getSimpleName(); }

    @Override
    protected void applyCatalanoFilter() {
        new ArtifactsRemoval(modelWinSize.getValue())
            .applyInPlace(imageFBmp);
    }

    @Override
    protected void makeOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxOptions = Box.createHorizontalBox();
        boxOptions.setBorder(BorderFactory.createTitledBorder("Adaptive contrast"));

        boxOptions.add(Box.createHorizontalGlue());
        boxOptions.add(makeSliderVert(modelWinSize, "WinSize", "Size of window"));
        boxOptions.add(Box.createHorizontalGlue());

        boxCenterLeft.add(boxOptions);

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
