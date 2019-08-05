package ksn.imgusage.utils;

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
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.opencv.core.Mat;
import org.slf4j.Logger;

import Catalano.Imaging.FastBitmap;
import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;

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

    public static void makeSliderVert(Box doAdd, ISliderModel<?> model, String title, String tip) {
        JLabel labTitle = new JLabel(title);
        labTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtValue = new JTextField();
        txtValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtValue.setHorizontalAlignment(JTextField.CENTER);
        txtValue.setMaximumSize(new Dimension(150, 40));

        JSlider slider = new JSlider(JSlider.VERTICAL);
        slider.setModel(model.getWrapped());
//        slider.setMajorTickSpacing(20);
//        slider.setMinorTickSpacing(4);
//        slider.setPaintTicks(true);
        slider  .setToolTipText(tip);
        txtValue.setToolTipText(tip);
        labTitle.setToolTipText(tip);

        Box boxColumn = Box.createVerticalBox();
        boxColumn.setBorder(BorderFactory.createTitledBorder(""));
        boxColumn.add(labTitle);
        boxColumn.add(slider);
        boxColumn.add(txtValue);

        doAdd.add(boxColumn);

        Runnable executor = () -> txtValue.setText(model.getFormatedText());
        executor.run();
        model.getWrapped().addChangeListener(ev -> executor.run());
        txtValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                handle();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handle();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                handle();
            }

            private void handle() {
                SwingUtilities.invokeLater(() -> {
                    String newVaue = txtValue.getText();
                    if (newVaue.equals(model.getFormatedText()))
                        return;

                    model.setFormatedText(newVaue);
                });
            }
        });
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
        btnAsBoost.setToolTipText("Speed up by reducing the image");
        btnAsBoost.addActionListener(ev -> {
            setter.accept(btnAsBoost.isSelected());
            executor.run();
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
            return ImgHelper.resize(image, newWidth, newHeight);
        }
        return image;
    }
    public static Mat boostImage(Mat image, Logger logger) {
        double zoomX = 400 / (double)image.width();
        double zoomY = 250 / (double)image.height();
        double zoom = Math.min(zoomX, zoomY);
        logger.trace("zoom={}", zoom);
        if (zoom < 1) {
            int newWidth  = (int)(zoom * image.width());
            int newHeight = (int)(zoom * image.height());
            return ImgHelper.resize(image, newWidth, newHeight);
        }
        return image;
    }

}
