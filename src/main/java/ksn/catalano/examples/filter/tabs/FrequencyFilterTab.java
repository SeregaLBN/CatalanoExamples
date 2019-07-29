package ksn.catalano.examples.filter.tabs;

import java.awt.Component;
import java.awt.Cursor;
import java.util.function.IntFunction;

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
            Box boxOptions = Box.createHorizontalBox();
            boxOptions.setBorder(BorderFactory.createTitledBorder("Frequency filter"));

            boxOptions.add(Box.createHorizontalGlue());
            JSlider sliderMin = makeSliderVert(boxOptions, "Min", modelMin, null);
            boxOptions.add(Box.createHorizontalStrut(8));
            JSlider sliderMax = makeSliderVert(boxOptions, "Max", modelMax, null);
            boxOptions.add(Box.createHorizontalGlue());

            boxCenterLeft.add(boxOptions);

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
        }
    }

    public static JSlider makeSliderVert(Box doAdd, String title, DefaultBoundedRangeModel model, IntFunction<String> valueTransformer) {
        JLabel labTitle = new JLabel(title); labTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labValue = new JLabel();      labValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider slider = new JSlider(JSlider.VERTICAL);
        slider.setModel(model);
//        slider.setMajorTickSpacing(20);
//        slider.setMinorTickSpacing(4);
//        slider.setPaintTicks(true);

        Box boxColumn = Box.createVerticalBox();
        boxColumn.setBorder(BorderFactory.createTitledBorder(""));
        boxColumn.add(labTitle);
        boxColumn.add(slider);
        boxColumn.add(labValue);

        doAdd.add(boxColumn);

        Runnable executor = () -> {
            int val = model.getValue();
            labValue.setText((valueTransformer == null)
                             ? Integer.toString(val)
                             : valueTransformer.apply(val));
        };
        executor.run();
        model.addChangeListener(ev -> executor.run());

        return slider;
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
