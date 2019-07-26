package ksn.catalano.examples.filter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

// https://github.com/DiegoCatalano/Catalano-Framework/releases
import Catalano.Imaging.FastBitmap;

public class FilterUsageExample {

    static {
        org.apache.log4j.BasicConfigurator.configure();
    }

    private static final Logger logger = Logger.getLogger(FilterUsageExample.class);
    private static final String DEFAULT_CAPTION = "Catalano demo filters";
    private static final int WIDTH_LEFT_PANEL = 150;

    private final JFrame frame;
    private JTabbedPane tabPanel;
    private BooleanSupplier isGray;
    private BooleanSupplier isScale;

    private File imageFile;
    private BufferedImage original;
    private List<FastBitmap> tabImages = new ArrayList<>();

    private Frame getOwner() { return null; }

    public FilterUsageExample() {
        frame = new JFrame(DEFAULT_CAPTION);
        initialize();
    }

    private void initialize() {
        /**/
        Object keyBind = "CloseFrame";
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        frame.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { FilterUsageExample.this.onClose(); }
        });
        /**/

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { FilterUsageExample.this.onClose(); }
        });

        frame.setResizable(true);
        createComponents();

        frame.pack();
        frame.setLocationRelativeTo(getOwner());
    }

    private void onCancel(ActionEvent ev) {
        logger.info("onCancel");
        onClose();
    }
    private void onClose() {
        frame.dispose();
        logger.warn("Good bay!");
    }

    private void onOriginalImageAsGray(JPanel imagePanel) {
        logger.trace("onOriginalImageAsGray");

        if (original == null)
            return;

        FastBitmap bmp = new FastBitmap(original);
        if (isGray.getAsBoolean() && !bmp.isGrayscale())
            bmp.toGrayscale();
        if (tabImages.isEmpty())
            tabImages.add(bmp);
        else
            tabImages.set(0, bmp);
        imagePanel.repaint();
    }

    private void onImagePanelPaint(JPanel imagePanel, Graphics2D g) {
        logger.trace("onImagePanelPaint");

        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0xAE, 0xD6, 0xF1));
        g.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());

        int i = tabPanel.getSelectedIndex();
        if (i < 0)
            return;
        if (i >= tabImages.size())
            return;

        FastBitmap img = tabImages.get(i);
        if (img == null)
            return;

        BufferedImage image = img.toBufferedImage();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        logger.trace("image.size={" + image.getWidth() + ", " + image.getHeight() + "}");
        logger.trace("panel.size={" + imagePanel.getWidth() + ", " + imagePanel.getHeight() + "}");

        if (isScale.getAsBoolean()) {
            double zoomX = imagePanel.getWidth()  / (double)image.getWidth();
            double zoomY = imagePanel.getHeight() / (double)image.getHeight();
            double zoom = Math.min(zoomX, zoomY);
            logger.trace("zoom=" + zoom);
            g.drawImage(image, 0,0, (int)(zoom * image.getWidth()), (int)(zoom * image.getHeight()), (ImageObserver)null);
        } else {
            g.drawImage(image, 0,0, (ImageObserver)null);
        }

    }

    private void onImagePanelMousePressed(MouseEvent ev) {
        logger.info("onImagePanelMousePressed");
    }

    private void createComponents() {
        tabPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPanel.setBorder(BorderFactory.createEmptyBorder(8,8,2,8));
        tabPanel.addChangeListener(this::onTabChanged);
        addFirstTab();

        frame.getContentPane().add(tabPanel, BorderLayout.CENTER);
    }

    private JPanel buildImagePanel() {
        JPanel[] tmp = { null };
        JPanel imagePanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                FilterUsageExample.this.onImagePanelPaint(tmp[0], (Graphics2D)g);
            }
        };
        tmp[0] = imagePanel;

        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                FilterUsageExample.this.onImagePanelMousePressed(ev);
            }
        });
        imagePanel.setMinimumSize(new Dimension(150, 200));
        imagePanel.setPreferredSize(new Dimension(150, 200));

        return imagePanel;
    }

    private void addFirstTab() {
        JPanel imagePanel = buildImagePanel();
        JPanel leftPanel = new JPanel();
        { // fill leftPanel
            Box boxCenterLeft = Box.createVerticalBox();
            { // fill boxCenterLeft
                boxCenterLeft.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

                JButton btnLoadImage = new JButton("Load image...");
                btnLoadImage.addActionListener(ev -> {
                    logger.trace("onSelectImage");

                    File file = selectImageFile(imageFile);
                    if (file == null)
                        return;

                    try {
                        original = ImageIO.read(file);
                    } catch (IOException ex) {
                        logger.error("Can`t read image", ex);
                        return;
                    }

                    imageFile = file;

                    // reset all filters
                    for (int i=0; i<tabImages.size(); ++i)
                        tabImages.set(i, null);
                    onOriginalImageAsGray(imagePanel); // apply
                });
                SwingUtilities.invokeLater(btnLoadImage::requestFocus);
                boxCenterLeft.add(btnLoadImage);

                boxCenterLeft.add(Box.createVerticalStrut(6));

                JCheckBox btnAsGray = new JCheckBox("Gray", false);
                btnAsGray.addActionListener(ev -> onOriginalImageAsGray(imagePanel));
                boxCenterLeft.add(btnAsGray);

                JCheckBox btnScale = new JCheckBox("Scale", true);
                btnScale.addActionListener(ev -> imagePanel.repaint());
                boxCenterLeft.add(btnScale);

                makeSameWidth(new Component[] { btnLoadImage, btnAsGray, btnScale });

                isGray  = btnAsGray::isSelected;
                isScale = btnScale ::isSelected;
            }

            Box boxBottomLeft = Box.createVerticalBox();
            { // fill boxBottomLeft
                boxBottomLeft.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
                JButton btnAddFilter = new JButton("Add filter");
                boxBottomLeft.add(btnAddFilter);

                boxBottomLeft.add(Box.createVerticalStrut(6));

                JButton btnCancel = new JButton("Cancel");
                btnCancel.addActionListener(this::onCancel);
                boxBottomLeft.add(btnCancel);

                makeSameWidth(new Component[] { btnAddFilter, btnCancel });
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
            tabPanel.addTab("Orignal", panel);
        }
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
        private static final List<String> ALL = Collections.unmodifiableList(Arrays.asList("jpeg", "jpg", "gif", "tiff", "tif", "png"));

        @Override
        public boolean accept(File file) {
            return file.isDirectory() || ALL.contains(FilenameUtils.getExtension(file.getName()));
        }

        @Override
        public String getDescription() {
            return "Image Only";
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.warn("", ex);
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                FilterUsageExample mainWin = new FilterUsageExample();
                mainWin.frame.setVisible(true);
            });
        } catch (Exception ex) {
            logger.error(FilterUsageExample.class.getSimpleName() + "::main", ex);
        }
    }

}
