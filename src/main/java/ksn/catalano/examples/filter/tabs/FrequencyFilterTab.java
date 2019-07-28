package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;

public class FrequencyFilterTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilterTab.class);
    private static final int MIN = 0;
    private static final int MAX = 200;

    private final ITabHandler tabHandler;
    private final ITab source;
    private FastBitmap image;
    DefaultBoundedRangeModel modelMin = new DefaultBoundedRangeModel(  0, 0, MIN, MAX);
    DefaultBoundedRangeModel modelMax = new DefaultBoundedRangeModel(100, 0, MIN, MAX);

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;
    }

    @Override
    public FastBitmap getImage() {
        if (image == null && source != null) {
            JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
            try {
                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                image = new FastBitmap(source.getImage());
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
        }
        return image;
    }


    @Override
    public void resetImage() {
        image = null;
    }

    public void makeTab() {
        FirstTab.makeTab(
             tabHandler,
             this,
             FrequencyFilter.class.getSimpleName(),
             true,
             this::makeFrequencyFilterOptions
         );
    }

    public void makeFrequencyFilterOptions(JPanel imagePanel, Box boxCenterLeft) {
        Box boxMinMax = Box.createHorizontalBox();
        JSlider sliderMin = new JSlider(JSlider.VERTICAL);
        JSlider sliderMax = new JSlider(JSlider.VERTICAL);
        sliderMin.setModel(modelMin);
        sliderMax.setModel(modelMax);
        boxMinMax.add(sliderMin);
        boxMinMax.add(Box.createHorizontalStrut(4));
        boxMinMax.add(sliderMax);

        boxCenterLeft.add(boxMinMax);
    }

}
