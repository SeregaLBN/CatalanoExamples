package ksn.catalano.examples.filter.tabs;

import java.awt.Cursor;
import java.util.Hashtable;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import Catalano.Imaging.Filters.Resize;

public class FrequencyFilterTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilterTab.class);
    private static final int MIN = 0;
    private static final int MAX = 200;

    private final ITabHandler tabHandler;
    private ITab source;
    private FastBitmap image;
    private boolean boosting = true;
    private Runnable imagePanelInvalidate;
    private DefaultBoundedRangeModel modelMin = new DefaultBoundedRangeModel(  0, 0, MIN, MAX);
    private DefaultBoundedRangeModel modelMax = new DefaultBoundedRangeModel(100, 0, MIN, MAX);
    private Timer timer;

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source) {
        this.tabHandler = tabHandler;
        this.source = source;
    }

    public FrequencyFilterTab(ITabHandler tabHandler, ITab source, int min, int max, boolean boosting) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.modelMin.setValue(min);
        this.modelMax.setValue(max);
        this.boosting = boosting;
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
            if (boosting) {
                double zoomX = 400 / (double)image.getWidth();
                double zoomY = 250 / (double)image.getHeight();
                double zoom = Math.min(zoomX, zoomY);
                logger.trace("zoom={}", zoom);
                if (zoom < 1) {
                    int newWidth  = (int)(zoom * image.getWidth());
                    int newHeight = (int)(zoom * image.getHeight());

                    Resize resize = new Resize(newWidth, newHeight);
                    image = resize.apply(image);
                }
            }
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
        imagePanelInvalidate = imagePanel::repaint;

        {
            JCheckBox btnAsBoost = new JCheckBox("Boosting", boosting);
            btnAsBoost.addActionListener(ev -> {
                boosting = btnAsBoost.isSelected();
                resetImage();
            });
            boxCenterLeft.add(btnAsBoost);
        }

        {
            JPanel panelGroup = new JPanel();
            panelGroup.setBorder(BorderFactory.createTitledBorder("Frequency filter"));

            Box boxAll = Box.createVerticalBox();

            Box boxMinMax = Box.createHorizontalBox();
            JSlider sliderMin = new JSlider(JSlider.VERTICAL);
            JSlider sliderMax = new JSlider(JSlider.VERTICAL);

            sliderMin.setModel(modelMin);
            sliderMax.setModel(modelMax);

            sliderMin.setToolTipText("Min");
            sliderMax.setToolTipText("Max");

            boxMinMax.add(sliderMin);
            boxMinMax.add(Box.createHorizontalStrut(4));
            boxMinMax.add(sliderMax);

            Hashtable<Integer, JLabel> positionMin = new Hashtable<>();
            Hashtable<Integer, JLabel> positionMax = new Hashtable<>();
            for (int i = MIN; i <= MAX; i += 35) {
                positionMin.put(i, new JLabel(Integer.toString(i)));
                positionMax.put(i, new JLabel(Integer.toString(i)));
            }
            positionMin.put(MIN, new JLabel("Min"));
            positionMax.put(MAX, new JLabel("Max"));

            sliderMin.setLabelTable(positionMin);
            sliderMax.setLabelTable(positionMax);

            modelMin.addChangeListener(ev -> {
                int valMin = modelMin.getValue();
                logger.trace("modelMin: value={}", valMin);
                if (valMin > sliderMax.getValue())
                    sliderMax.setValue(valMin);
                debounceResetImage();
            });
            modelMax.addChangeListener(ev -> {
                int valMax = modelMax.getValue();
                logger.trace("modelMax: value={}", valMax);
                if (valMax < sliderMin.getValue())
                    sliderMin.setValue(valMax);
                debounceResetImage();
            });

            boxAll.add(boxMinMax);
            boxCenterLeft.add(boxAll);
        }
    }

    private void debounceResetImage() {
        if (timer == null)
            timer = new Timer(300, ev -> {
                timer.stop();
                logger.info("debounce: call resetImage");
                resetImage();
            });

        if (timer.isRunning())
            timer.restart();
        else
            timer.start();
    }

}
