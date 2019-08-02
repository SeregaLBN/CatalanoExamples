package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import ksn.catalano.examples.filter.model.SliderIntModel;
import ksn.catalano.examples.filter.util.UiHelper;

public class FrequencyFilterTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilterTab.class);
    private static final int MIN = 0;
    private static final int MAX = 1024;

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private SliderIntModel modelMin = new SliderIntModel(  0, 0, MIN, MAX);
    private SliderIntModel modelMax = new SliderIntModel(100, 0, MIN, MAX);
    private Timer timer;

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;

        makeTab();
    }

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source, boolean boosting, int min, int max) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelMin.setValue(min);
        this.modelMax.setValue(max);
        this.boosting = boosting;

        makeTab();
    }

    @Override
    public FastBitmap getImage() {
        if (image != null)
            return image;
        if (source == null)
            return null;

        image = source.getImage();
        if (image == null)
            return null;

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            image = new FastBitmap(image);
            if (boosting)
                image = UiHelper.boostImage(image, logger);
            if (!image.isGrayscale())
                image.toGrayscale();

            FourierTransform fourierTransform = new FourierTransform(image);
            fourierTransform.Forward();

            FrequencyFilter frequencyFilter = new FrequencyFilter(modelMin.getValue(), modelMax.getValue());
            frequencyFilter.ApplyInPlace(fourierTransform);

            fourierTransform.Backward();
            image = fourierTransform.toFastBitmap();
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
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
             FrequencyFilter.class.getSimpleName(),
             true,
             this::makeFilterOptions
         );
    }

    public void makeFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        boxCenterLeft.add(UiHelper.makeAsBoostCheckBox(() -> boosting, b -> boosting = b, this::resetImage));

        {
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Frequency filter"));

            boxOptions.add(Box.createHorizontalGlue());
            UiHelper.makeSliderVert(boxOptions, modelMin, "Min", "Minimum value for to keep");
            boxOptions.add(Box.createHorizontalStrut(8));
            UiHelper.makeSliderVert(boxOptions, modelMax, "Max", "Maximum value for to keep");
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

            modelMin.getWrapped().addChangeListener(ev -> {
                logger.trace("modelMin: value={}", modelMin.getFormatedText());
                int valMin = modelMin.getValue();
                if (valMin > modelMax.getValue())
                    modelMax.setValue(valMin);
                debounceResetImage();
            });
            modelMax.getWrapped().addChangeListener(ev -> {
                logger.trace("modelMax: value={}", modelMax.getFormatedText());
                int valMax = modelMax.getValue();
                if (valMax < modelMin.getValue())
                    modelMin.setValue(valMax);
                debounceResetImage();
            });
        }
    }

    private void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

}
