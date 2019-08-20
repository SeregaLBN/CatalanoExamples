package ksn.imgusage.tabs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.SelectFilterDialog;
import ksn.imgusage.utils.UiHelper;

/** Abstract tab. Contains common methods / shared logic. */
public abstract class BaseTab<TTabParams extends ITabParams> implements ITab<TTabParams> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final int WIDTH_LEFT_PANEL = 300;

    protected ITabHandler tabHandler;
    protected ITab<?> source;
    protected BufferedImage image;
    private Runnable imagePanelRepaint;
    private Timer timer;

    @Override
    public void setHandler(ITabHandler tabHandler) {
        this.tabHandler = tabHandler;
    }

    @Override
    public void setSource(ITab<?> newSource) {
        this.source = newSource;
        resetImage();
    }

    protected BufferedImage getSourceImage() {
        if (source == null)
            return null;

        return source.getImage();
    }

    @Override
    public BufferedImage getImage() {
        if (image != null)
            return image;

        BufferedImage src = getSourceImage();
        if (src == null)
            return null;

        JFrame frame = tabHandler.getFrame();
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                applyFilter();
            } catch (Exception ex) {
                logger.error(ex.toString());
                image = ImgHelper.failedImage();
                tabHandler.onError(ex.getMessage(), this, null);
            }
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }

    protected abstract void applyFilter();


    @Override
    public void resetImage() {
        if (image == null) {
            logger.trace("> resetImage: already reseted");
        } else {
            logger.trace("> resetImage: reset...");
            image = null;
        }
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::repaintImage, logger);
    }

    private void repaintImage() {
        logger.trace("  repaintImage: repaint panel");
        if (imagePanelRepaint != null)
            imagePanelRepaint.run();
        SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(this));
    }

    protected final JButton makeButtonAddFilter() {
        JButton btnAddFilter = new JButton("Add filter...");
        btnAddFilter.addActionListener(ev -> tabHandler.onAddNewFilter());
        return btnAddFilter;
    }
    protected final JButton makeButtonRemoveFilter() {
        JButton btnRemoveFilter = new JButton("Remove filter");
        btnRemoveFilter.addActionListener(ev -> tabHandler.onRemoveFilter(this));
        return btnRemoveFilter;
    }
    protected final JButton makeButtonSaveImage() {
        JButton btn = new JButton("Save to png...");
        btn.addActionListener(ev -> {
            File file = UiHelper.chooseFileToSavePngImage(btn, tabHandler.getCurrentDir());
            if (file == null)
                return; // canceled
            file = SelectFilterDialog.checkExtension(file, ".png");
            try {
                boolean succ = ImageIO.write(image, "png", file);
                if (succ)
                    logger.info("Image saved to PNG file {}", file);
                else
                    logger.warn("Can`t save image to PNG file {}", file);
            } catch (Exception ex) {
                logger.error("Can`t save image to PNG file: {}", ex);
                tabHandler.onError("Can`t save image to PNG file: " + ex, this, btn);
            }
        });
        return btn;
    }

    protected Component makeUpButtons() {
        return null;
    }
    protected abstract Component makeOptions();
    protected Component makeDownButtons() {
        Box box4Buttons = Box.createHorizontalBox();
        box4Buttons.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        box4Buttons.add(makeButtonAddFilter());
        box4Buttons.add(Box.createHorizontalStrut(3));
        box4Buttons.add(makeButtonRemoveFilter());
        box4Buttons.add(Box.createHorizontalGlue());
        box4Buttons.add(makeButtonSaveImage());

        return box4Buttons;
    }

    protected final Component makeTab() {
        JPanel imagePanel = buildImagePanel(tabHandler);
        JPanel leftPanel = new JPanel();
        { // fill leftPanel
            leftPanel.setLayout(new BorderLayout());

            Component upButtons = makeUpButtons();
            if (upButtons != null)
                leftPanel.add(upButtons    , BorderLayout.NORTH);
            leftPanel.add(makeOptions()    , BorderLayout.CENTER);
            leftPanel.add(makeDownButtons(), BorderLayout.SOUTH);

            leftPanel.setMinimumSize  (new Dimension(WIDTH_LEFT_PANEL, 200));
            leftPanel.setPreferredSize(new Dimension(WIDTH_LEFT_PANEL, -1));
        }

        // make root tab panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(leftPanel , BorderLayout.EAST);

        return panel;
    }

    private JPanel buildImagePanel(ITabHandler tabHandler) {
        JPanel[] tmp = { null };
        JPanel imagePanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                logger.trace("ImagePanel.paintComponent");
                tabHandler.onImagePanelPaint(tmp[0], (Graphics2D)g);
            }
        };
        tmp[0] = imagePanel;

        imagePanel.setMinimumSize(new Dimension(150, 200));
        imagePanel.setPreferredSize(new Dimension(700, 400));

        imagePanelRepaint = imagePanel::repaint;
        return imagePanel;
    }

    protected static Container makeSliderVert(ISliderModel<?> model, String title, String tip) {
        JLabel labTitle = new JLabel(title);
        labTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtValue = new JTextField();
        txtValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtValue.setHorizontalAlignment(JTextField.CENTER);
        txtValue.setMaximumSize(new Dimension(150, 40));

        JSlider slider = new JSlider(JSlider.VERTICAL);
        slider.setModel(model.getWrapped());
        if (tip != null) {
            slider  .setToolTipText(tip);
            txtValue.setToolTipText(tip);
            labTitle.setToolTipText(tip);
        }

        Box boxColumn = Box.createVerticalBox();
        boxColumn.setBorder(BorderFactory.createTitledBorder(""));
        boxColumn.add(labTitle);
        boxColumn.add(slider);
        boxColumn.add(txtValue);

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

        return boxColumn;
    }

    protected static Container makeEditBox(ISliderModel<?> model, String title, String tip) {
        JLabel labTitle = new JLabel(title + ": ");
        labTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        labTitle.setToolTipText(tip);

        JTextField txtValue = new JTextField();
        txtValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtValue.setHorizontalAlignment(JTextField.CENTER);
        txtValue.setMaximumSize(new Dimension(150, 40));
        txtValue.setToolTipText(tip);
        Dimension prefSize = txtValue.getPreferredSize();
        prefSize.width = 45;
        txtValue.setPreferredSize(prefSize);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder(""));
        box.setToolTipText(tip);

        box.add(Box.createHorizontalStrut(8));
        box.add(labTitle);
        box.add(Box.createHorizontalStrut(2));
        box.add(txtValue);
        box.add(Box.createHorizontalGlue());

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

        return box;
    }

}
