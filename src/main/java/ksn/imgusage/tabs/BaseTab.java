package ksn.imgusage.tabs;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.model.ISliderModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.utils.ImgHelper;
import ksn.imgusage.utils.SelectFilterDialog;
import ksn.imgusage.utils.UiHelper;

/** Abstract tab. Contains common methods / shared logic. */
public abstract class BaseTab<TTabParams extends ITabParams> implements ITab<TTabParams> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final int WIDTH_LEFT_PANEL = 300;

    protected static final int DEFAULT_WIDTH  = 300;
    protected static final int DEFAULT_HEIGHT = 200;

    private static final int DEBOUNCE_TIMEOUT_MS = 150;

    protected ITabHandler tabHandler;
    protected ITab<?> source;
    protected BufferedImage image;
    protected Runnable imagePanelRepaint;
    private Timer timer;

    @Override
    public void setHandler(ITabHandler tabHandler) {
        this.tabHandler = tabHandler;
    }

    @Override
    public void setSource(ITab<?> newSource) {
        if (this.source == newSource) // ref eq
            return;

        this.source = newSource;
        resetImage();
    }

    protected BufferedImage getSourceImage() {
        if (source == null)
            return null;

        return source.getImage();
    }

    @Override
    public BufferedImage getDrawImage() {
        return getImage();
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
                logger.trace("getImage: applyFilter...");
                applyFilter();
                logger.trace("getImage: ...applyFilter");
            } catch (Exception ex) {
                logger.error("getImage: {}", ex);
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
    public void resetImage(boolean debounce) {
        if (image == null) {
//            logger.trace("> resetImage: already reseted");
        } else {
//            logger.trace("> resetImage: reset...");
            image = null;
        }
        if (debounce)
            UiHelper.debounceExecutor(() -> timer, t -> timer = t, DEBOUNCE_TIMEOUT_MS, this::repaintImage, logger);
        else
            repaintImage();
    }
    @Override
    public void resetImage() {
        resetImage(true);
    }

    private void repaintImage() {
        //logger.trace("  repaintImage: mark to repaint panel");
        if (imagePanelRepaint != null)
            imagePanelRepaint.run();

        //SwingUtilities.invokeLater(() -> tabHandler.onImageChanged(source, this));
        tabHandler.onImageChanged(this);
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
            file = SelectFilterDialog.checkExtension(file, "png");
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
                tabHandler.onImgPanelDraw(tmp[0], (Graphics2D)g, logger);
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

    protected static Container makeEditBox(ISliderModel<?> model, String title, String borderTitle, String tip) {
        java.util.List<Consumer<String>> setterTextList = new ArrayList<>(1);
        Container res = makeEditBox(
            setterTextList::add,
            newValue  -> {
                if (newValue.equals(model.getFormatedText()))
                    return;

                model.setFormatedText(newValue);
            },
            title, borderTitle, tip);
        setterTextList.get(0).accept(model.getFormatedText());
        return res;
    }

    protected static Container makeEditBox(Consumer<Consumer<String>> setterTextExternal, Consumer<String> setterTextInternal, String labelText, String borderTitle, String tip) {
        JLabel labTitle = new JLabel(labelText + ": ");
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
        if (borderTitle != null)
            box.setBorder(BorderFactory.createTitledBorder(borderTitle));
        box.setToolTipText(tip);

        box.add(Box.createHorizontalStrut(8));
        box.add(labTitle);
        box.add(Box.createHorizontalStrut(2));
        box.add(txtValue);
        box.add(Box.createHorizontalGlue());

        setterTextExternal.accept(txtValue::setText);

        if (setterTextInternal == null) {
            txtValue.setEditable(false);
            return box;
        }

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
                SwingUtilities.invokeLater(() -> setterTextInternal.accept(txtValue.getText()));
            }
        });

        return box;
    }

    public <N extends Number> Component makePoint(ISliderModel<N> modelPointX, ISliderModel<N> modelPointY, String borderTitle, String tip, String tipX, String tipY) {
        Box boxSize = Box.createHorizontalBox();
        boxSize.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (tip != null)
            boxSize.setToolTipText(tip);
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(makeSliderVert(modelPointX, "X", tipX));
        boxSize.add(Box.createHorizontalStrut(2));
        boxSize.add(makeSliderVert(modelPointY, "Y", tipY));
        boxSize.add(Box.createHorizontalGlue());
        return boxSize;
    }

    public Component makeSize(SliderIntModel modelSizeW, SliderIntModel modelSizeH, String borderTitle, String tip, String tipWidth, String tipHeight) {
        Box boxSize = Box.createHorizontalBox();
        boxSize.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (tip != null)
            boxSize.setToolTipText(tip);
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(makeSliderVert(modelSizeW, "Width", tipWidth));
        boxSize.add(Box.createHorizontalStrut(2));
        boxSize.add(makeSliderVert(modelSizeH, "Height", tipHeight));
        boxSize.add(Box.createHorizontalGlue());
        return boxSize;
    }

    protected JCheckBox makeCheckBox(BooleanSupplier getter, Consumer<Boolean> setter, String title, String paramName, String tip, Runnable customListener) {
        JCheckBox checkBox = new JCheckBox(title, getter.getAsBoolean());
        if (tip != null)
            checkBox.setToolTipText(tip);
        checkBox.addItemListener(ev -> {
            boolean checked = (ev.getStateChange() == ItemEvent.SELECTED);
            setter.accept(checked);
            logger.trace("{} is {}", paramName, checked ? "checked" : "unchecked");
            if (customListener != null)
                customListener.run();
            resetImage();
        });
        return checkBox;
    }

    protected Box makeBoxedCheckBox(BooleanSupplier getter, Consumer<Boolean> setter, String borderTitle, String checkBoxTitle, String paramName, String tip, Runnable customListener) {
        Box box = Box.createVerticalBox();
        if (borderTitle != null)
            box.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (tip != null)
            box.setToolTipText(tip);
        box.add(makeCheckBox(getter, setter, checkBoxTitle, paramName, tip, customListener));
        return box;
    }

    protected <E extends Enum<?>> Stream<JRadioButton> makeRadioButtons(
        Stream<E> values,
        Supplier<E> getter,
        Consumer<E> setter,
        String paramName,
        Function<E, String> radioText,
        Function<E, String> radioTip,
        Consumer<E> customListener
    ) {
        ButtonGroup radioGroup = new ButtonGroup();
        E initVal = getter.get();
        return values.map(e -> {
            JRadioButton radioBtn = new JRadioButton(
                radioText == null
                    ? e.name()
                    : radioText.apply(e),
                e == initVal);
            if (radioTip != null)
                radioBtn.setToolTipText(radioTip.apply(e));
            radioBtn.addItemListener(ev -> {
                if (ev.getStateChange() == ItemEvent.SELECTED) {
                    setter.accept(e);
                    logger.trace("{} changed to {}", paramName, e);
                    if (customListener != null)
                        customListener.accept(e);;
                    resetImage();
                }
            });
            radioGroup.add(radioBtn);
            return radioBtn;
        });
    }

    protected <E extends Enum<?>> Box makeBoxedRadioButtons(
        Stream<E> values,
        Supplier<E> getter,
        Consumer<E> setter,
        String borderTitle,
        String paramName,
        String boxTip,
        Function<E, String> radioText,
        Function<E, String> radioTip,
        Consumer<E> customListener
    ) {
        Box box = Box.createVerticalBox();
        if (borderTitle != null)
            box.setBorder(BorderFactory.createTitledBorder(borderTitle));
        if (boxTip != null)
            box.setToolTipText(boxTip);
        makeRadioButtons(values, getter, setter, paramName, radioText, radioTip, customListener)
            .forEach(box::add);
        return box;
    }

    protected <E extends Enum<?>> Component makeComboBox(E[] values, Supplier<E> getter, Consumer<E> setter, String name, String title, String tip) {
        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        JComboBox<E> comboBox = new JComboBox<>(values);
        comboBox.setSelectedItem(getter.get());
        if (tip != null)
            comboBox.setToolTipText(tip);
        comboBox.addActionListener(ev -> {
            @SuppressWarnings("unchecked")
            E newValue = (E)comboBox.getSelectedItem();
            setter.accept(newValue);
            logger.trace("{} changed to {}", name, newValue);
            resetImage();
        });
        box.add(comboBox);
        return box;
    }

    protected void addChangeListenerDiff1WithModels(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        assert model.getMinimum() == modelToCheck.getMinimum().intValue();
        assert model.getMaximum() == modelToCheck.getMaximum().intValue();
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            Integer myVal = model.getValue();
            Integer checkVal = modelToCheck.getValue();
            if (checkMax) {
                if (myVal >= checkVal)
                    modelToCheck.setValue(myVal + 1);
                else
                    applyValueParams.run();
            } else {
                if (myVal <= checkVal)
                    modelToCheck.setValue(myVal - 1);
                else
                    applyValueParams.run();
            }
            resetImage();
        });
    }

    protected void addChangeListenerDiff1WithModelsSumm(String name, ISliderModel<Integer> model, boolean checkMax, ISliderModel<Integer> modelToCheck, Runnable applyValueParams) {
        assert model.getMinimum() == modelToCheck.getMinimum().intValue();
        assert model.getMinimum() == 0;
        assert model.getMaximum() == modelToCheck.getMaximum().intValue();
        int max = model.getMaximum();
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            Integer myVal = model.getValue();
            Integer checkVal = modelToCheck.getValue();
            if ((myVal + checkVal) >= max)
                modelToCheck.setValue(max - myVal - 1);
            else
                applyValueParams.run();
            resetImage();
        });
    }

    protected <N extends Number> void addChangeListener(String name, ISliderModel<N> model, Consumer<N> setter, Runnable customExecutor) {
        model.getWrapped().addChangeListener(ev -> {
            logger.trace("{}: value={}", name, model.getFormatedText());
            setter.accept(model.getValue());
            if (customExecutor != null)
                customExecutor.run();
            resetImage();
        });
    }

    protected <N extends Number> void addChangeListener(String name, ISliderModel<N> model, Consumer<N> setter) {
        addChangeListener(name, model, setter, null);
    }

}
