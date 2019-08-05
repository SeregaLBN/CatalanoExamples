package ksn.imgusage.tabs.opencv;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;

import Catalano.Imaging.Filters.AdaptiveContrastEnhancement;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.utils.ImgWrapper;
import ksn.imgusage.utils.UiHelper;

public class AsIsTab implements ITab {

    //private static final Logger logger = LoggerFactory.getLogger(AsIsTab.class);

    private final ITabHandler tabHandler;
    private ITab source;
    private Mat mat;
    private Runnable imagePanelInvalidate;

    public AsIsTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    @Override
    public ImgWrapper getImage() {
        if (mat != null)
            return new ImgWrapper(mat);
        if (source == null)
            return null;

        ImgWrapper wrp = source.getImage();
        if (wrp == null)
            return null;

        mat = wrp.getMat();

        return new ImgWrapper(mat);
    }


    @Override
    public void resetImage() {
        if (mat == null)
            return;

        mat = null;
        imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        this.source = newSource;
        resetImage();
    }

    private void makeTab() {
        UiHelper.makeTab(
             tabHandler,
             this,
             AdaptiveContrastEnhancement.class.getSimpleName(),
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;
    }

}
