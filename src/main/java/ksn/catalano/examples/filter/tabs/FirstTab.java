package ksn.catalano.examples.filter.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;

public class FirstTab implements ITab {

    private static final Logger logger = LoggerFactory.getLogger(FirstTab.class);
    private static final int WIDTH_LEFT_PANEL = 250;

    public static final String PROPERTY_NAME_SOURCE = "Source";
    public static final File DEFAULT_IMAGE = Paths.get("./exampleImages", "1024px-VolodimirHillAndDnieper.jpg").toFile();

    private final ITabHandler tabHandler;

    private BufferedImage source;
    private FastBitmap image;
    private File latestImageDir;
    private Runnable imagePanelInvalidate;

    private boolean isGray = false;
    private boolean isScale = true;


    public FirstTab(ITabHandler tabHandler) {
        this.tabHandler = tabHandler;

        latestImageDir = DEFAULT_IMAGE.getParentFile();
        if (!latestImageDir.exists())
            latestImageDir = null;
    }
    public FirstTab(
        ITabHandler tabHandler,
        File imageFile,
        boolean isGray,
        boolean isScale
    ) {
        this.tabHandler = tabHandler;
        this.isGray  = isGray;
        this.isScale = isScale;
        if (readImageFile(imageFile))
            latestImageDir = imageFile.getParentFile();
    }

    public boolean isScale() {
        return isScale;
    }

    public BufferedImage getSource() {
        return source;
    }

    @Override
    public FastBitmap getImage() {
        if (source == null)
            return null;

        if (image == null) {
            image = new FastBitmap(source);
            if (isGray && !image.isGrayscale())
                image.toGrayscale();
        }
        return image;
    }


    @Override
    public void resetImage() {
        if (image == null)
            return;

        image = null;
        if (imagePanelInvalidate != null)
            imagePanelInvalidate.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    @Override
    public void updateSource(ITab newSource) {
        throw new UnsupportedOperationException("Illegal call");
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
                    makeSameWidth(new Component[] { btnAddFilter, btnRemoveFilter });
                } else {
                    JButton btnCancel = new JButton("Cancel");
                    btnCancel.addActionListener(ev -> tabHandler.onCancel());
                    boxBottomLeft.add(btnCancel);
                    makeSameWidth(new Component[] { btnAddFilter, btnCancel });
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

    public void makeTab() {
        makeTab(
            tabHandler,
            this,
            "Original",
            false,
            this::makeLoadGrayScaleOptions
        );
    }


    private boolean readImageFile(File imageFile) {
        if (imageFile == null)
            return false;

        try {
            source = ImageIO.read(imageFile);
            resetImage();
            return true;
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
            return false;
        }
    }

    public void makeLoadGrayScaleOptions(JPanel imagePanel, Box boxCenterLeft) {
        imagePanelInvalidate = imagePanel::repaint;

        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> {
            logger.trace("onSelectImage");

            File file = selectImageFile(latestImageDir);
            if (!readImageFile(file))
                return;

            latestImageDir = file.getParentFile();
            imagePanel.repaint();
        });
        SwingUtilities.invokeLater(btnLoadImage::requestFocus);
        if (source == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);
        boxCenterLeft.add(btnLoadImage);

        boxCenterLeft.add(Box.createVerticalStrut(6));

        JCheckBox btnAsGray = new JCheckBox("Gray", isGray);
        btnAsGray.addActionListener(ev -> {
            isGray  = btnAsGray.isSelected();
            resetImage();
        });
        boxCenterLeft.add(btnAsGray);

        JCheckBox btnScale = new JCheckBox("Scale", isScale);
        btnScale.addActionListener(ev -> {
            isScale = btnScale.isSelected();
            imagePanel.repaint();
        });
        boxCenterLeft.add(btnScale);

        makeSameWidth(new Component[] { btnLoadImage, btnAsGray, btnScale });
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

    void onTabChanged(ChangeEvent ev) {
        logger.info("onTabChanged");
    }

    private static File selectImageFile(File oldFile) {
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

}
