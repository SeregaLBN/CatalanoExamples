package ksn.imgusage.tabs;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.UiHelper;

public abstract class BaseTab implements ITab {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final int WIDTH_LEFT_PANEL = 250;

    protected final ITabHandler tabHandler;
    protected ITab source;
    protected BufferedImage image;
    protected Boolean boosting;
    protected boolean addRemoveFilterButton = true;
    private Runnable imagePanelInvalidate;
    private Timer timer;

    protected BaseTab(ITabHandler tabHandler, ITab source, Boolean boosting) {
        this.tabHandler = tabHandler;
        this.source = source;
        this.boosting = boosting;
    }

    public abstract String getTabName();

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

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(tabHandler.getTabPanel());
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                applyFilter();
            } catch (Exception ex) {
                logger.error(ex.toString());
                image = ImgHelper.failedImage();
            }
        } finally {
            frame.setCursor(Cursor.getDefaultCursor());
        }
        return image;
    }

    protected abstract void applyFilter();


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
        this.source = newSource;
        resetImage();
    }

    protected final void makeTab() {
        JPanel imagePanel = buildImagePanel(tabHandler);
        JPanel leftPanel = new JPanel();
        { // fill leftPanel
            Box boxCenterLeft = Box.createVerticalBox();
            { // fill boxCenterLeft
                boxCenterLeft.setBorder(BorderFactory.createTitledBorder(""));
                if (boosting != null)
                    boxCenterLeft.add(makeAsBoostCheckBox());
                makeOptions(imagePanel, boxCenterLeft);
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
                    btnRemoveFilter.addActionListener(ev -> tabHandler.onRemoveFilter(this));
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
            tabPane.addTab(getTabName(), panel);
            tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        }
    }

    private JPanel buildImagePanel(ITabHandler tabHandler) {
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

        imagePanelInvalidate = imagePanel::repaint;
        return imagePanel;
    }

    protected abstract void makeOptions(JPanel imagePanel, Box boxCenterLeft);

    protected void debounceResetImage() {
        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 300, this::resetImage, logger);
    }

    private Box makeAsBoostCheckBox() {
        Box box = Box.createHorizontalBox();
        JCheckBox btnAsBoost = new JCheckBox("Boosting", this.boosting);
        btnAsBoost.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsBoost.setToolTipText("Speed up by reducing the image");
        btnAsBoost.addActionListener(ev -> {
            this.boosting = btnAsBoost.isSelected();
            resetImage();
        });
        box.add(btnAsBoost);
        return box;
    }

    protected static Component makeSliderVert(ISliderModel<?> model, String title, String tip) {
        JLabel labTitle = new JLabel(title);
        labTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtValue = new JTextField();
        txtValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtValue.setHorizontalAlignment(JTextField.CENTER);
        txtValue.setMaximumSize(new Dimension(150, 40));

        JSlider slider = new JSlider(JSlider.VERTICAL);
        slider.setModel(model.getWrapped());
        slider  .setToolTipText(tip);
        txtValue.setToolTipText(tip);
        labTitle.setToolTipText(tip);

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

}
