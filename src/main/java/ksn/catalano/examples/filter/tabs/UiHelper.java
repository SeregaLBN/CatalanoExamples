package ksn.catalano.examples.filter.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Resize;

public final class UiHelper {
    private UiHelper() {}

    private static final int WIDTH_LEFT_PANEL = 250;

    public static void makeTab(
        ITabHandler tabHandler,
        ITab self,
        String tabTitle,
        boolean addRemoveFilterButton,
        BiConsumer<JPanel, Box> fillCoxCenterLeft
    ) {
        JPanel imagePanel = buildImagePanel(tabHandler);
        JPanel leftPanel = new JPanel();
        { // fill leftPanel
            Box boxCenterLeft = Box.createVerticalBox();
            { // fill boxCenterLeft
                boxCenterLeft.setBorder(BorderFactory.createTitledBorder(""));
                fillCoxCenterLeft.accept(imagePanel, boxCenterLeft);
            }

            Box boxBottomLeft = Box.createVerticalBox();
            { // fill boxBottomLeft
                boxBottomLeft.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

                JButton btnAddFilter = new JButton("Add filter");
                btnAddFilter.addActionListener(ev -> tabHandler.onAddNewFilter());
                boxBottomLeft.add(btnAddFilter);

                boxBottomLeft.add(Box.createVerticalStrut(6));

                if (addRemoveFilterButton) {
                    JButton btnRemoveFilter = new JButton("Remove filter");
                    btnRemoveFilter.addActionListener(ev -> tabHandler.onRemoveFilter(self));
                    boxBottomLeft.add(btnRemoveFilter);
                    UiHelper.makeSameWidth(new Component[] { btnAddFilter, btnRemoveFilter });
                } else {
                    JButton btnCancel = new JButton("Cancel");
                    btnCancel.addActionListener(ev -> tabHandler.onCancel());
                    boxBottomLeft.add(btnCancel);
                    UiHelper.makeSameWidth(new Component[] { btnAddFilter, btnCancel });
                }
            }

            leftPanel.setLayout(new BorderLayout());
            leftPanel.add(boxCenterLeft, BorderLayout.CENTER);
            leftPanel.add(boxBottomLeft, BorderLayout.SOUTH);
            leftPanel.setMinimumSize(new Dimension(WIDTH_LEFT_PANEL, 200));
            leftPanel.setPreferredSize(new Dimension(WIDTH_LEFT_PANEL, -1));
        }

        { // make root tab panel
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(imagePanel, BorderLayout.CENTER);
            panel.add(leftPanel, BorderLayout.EAST);
            JTabbedPane tabPane = tabHandler.getTabPanel();
            tabPane.addTab(tabTitle, panel);
            tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        }
    }

    private static JPanel buildImagePanel(ITabHandler tabHandler) {
        JPanel[] tmp = { null };
        JPanel imagePanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                tabHandler.onImagePanelPaint(tmp[0], (Graphics2D)g);
            }
        };
        tmp[0] = imagePanel;

        imagePanel.setMinimumSize(new Dimension(150, 200));
        imagePanel.setPreferredSize(new Dimension(700, 400));

        return imagePanel;
    }


    public static void makeSameWidth(Component[] components) {
        int maxSizePos = findMaximumPreferredWidthPosition(components);
        int maxWidth = components[maxSizePos].getPreferredSize().width;

        for (Component component : components) {
            Dimension dim;
            dim = component.getPreferredSize(); dim.width = maxWidth; component.setPreferredSize(dim);
            dim = component.getMinimumSize  (); dim.width = maxWidth; component.setMinimumSize  (dim);
            dim = component.getMaximumSize  (); dim.width = maxWidth; component.setMaximumSize  (dim);
        }
    }
    private static int findMaximumPreferredWidthPosition(Component[] array) {
        int pos = 0;
        for (int i=1; i<array.length; i++)
            if (array[i].getPreferredSize().width > array[pos].getPreferredSize().width)
                pos = i;
        return pos;
    }

    public static File selectImageFile(File oldFile) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (oldFile != null)
            fileChooser.setCurrentDirectory(oldFile);

        int option = fileChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();

        return null;
    }

    private static class ImageFilter extends FileFilter {
        private static final List<String> ALL = Arrays.asList("jpeg", "jpg", "gif", "tiff", "tif", "png");

        @Override
        public boolean accept(File file) {
            if (file.isDirectory())
                return true;
            String fileName = file.getName();
            int pos = fileName.lastIndexOf('.');
            if (pos < 0)
                return false;
            String ext = fileName.substring(pos + 1);
            return ALL.stream().anyMatch(ext::equalsIgnoreCase);
        }

        @Override
        public String getDescription() {
            return "Image Only";
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

    public static void debounceExecutor(Supplier<Timer> getterTimer, Consumer<Timer> setterTimer, int debounceTimer, Runnable executor, Logger logger) {
        Timer timer = getterTimer.get();
        if (timer == null) {
            Timer[] wrapper = { null };
            wrapper[0] = timer = new Timer(debounceTimer, ev -> {
                wrapper[0].stop();
                logger.info("debounce: call resetImage");
                executor.run();
            });
            setterTimer.accept(timer);
        }

        if (timer.isRunning())
            timer.restart();
        else
            timer.start();
    }

    public static Box makeAsBoostCheckBox(BooleanSupplier getter, Consumer<Boolean> setter, Runnable executor) {
        Box box = Box.createHorizontalBox();
        JCheckBox btnAsBoost = new JCheckBox("Boosting", getter.getAsBoolean());
        btnAsBoost.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsBoost.addActionListener(ev -> {
            setter.accept(btnAsBoost.isSelected());
            executor.run();;
        });
        box.add(btnAsBoost);
        return box;
    }

    public static FastBitmap boostImage(FastBitmap image, Logger logger) {
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
        return image;
    }

}
