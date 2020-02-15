package ksn.imgusage.tabs;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.*;

import ksn.imgusage.filtersdemo.ImageFilterExamples;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.utils.UiHelper;

/** The first tab to select an image to work with. */
public class FirstTab extends BaseTab<FirstTabParams> {

    public static final File DEFAULT_IMAGE = Paths.get("exampleImages", "VolodHill.jpg").toAbsolutePath().toFile();

    public static final String TAB_TITLE = "Original";
    public static final String TAB_NAME  = "FirstTab";
    public static final String TAB_DESCRIPTION = "The first tab to select an image to work with";


    private BufferedImage sourceImage;
    private File latestImageDir = DEFAULT_IMAGE.getParentFile();
    private FirstTabParams params;
    private Consumer<String> showImageSize;

    @Override
    public Component makeTab(FirstTabParams params) {
        if (params == null)
            params = new FirstTabParams(DEFAULT_IMAGE, true);

        this.params = params;

        readImageFile(params.imageFile);
        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getGroup() { return null; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected BufferedImage getSourceImage() {
        return sourceImage;
    }

    public File getLatestImageDir() {
        return latestImageDir;
    }

    public boolean isScale() {
        return params.useScale;
    }

    @Override
    protected void applyFilter() {
        image = sourceImage;
        showImageSize.accept(image.getWidth() + "x" + image.getHeight());
    }

    public void onSelectImage() {
        logger.trace("onSelectImage");

        File file = UiHelper.chooseFileToLoadImage(JOptionPane.getRootFrame(), latestImageDir);
        readImageFile(file);
    }

    private void readImageFile(File imageFile) {
        if (imageFile == null)
            return;

        try {
            if (!imageFile.exists()) {
                logger.warn("File not found: {}", imageFile);
                tabHandler.onError(new Exception("File not found: " + imageFile), this, null);
                return;
            }
            sourceImage = ImageIO.read(imageFile);

            params.imageFile = imageFile;
            tabHandler.getFrame().setTitle(ImageFilterExamples.DEFAULT_TITLE + ": " + imageFile.getName());
            latestImageDir = imageFile.getParentFile();

            invalidateAsync();
        } catch (IOException ex) {
            logger.error("Can`t read image", ex);
        }
    }

    private final JButton makeButtonLoadPipeline() {
        JButton btnLoad = new JButton("Load pipeline...");
        btnLoad.setToolTipText("Load image pipeline tabs");
        btnLoad.addActionListener(ev -> tabHandler.onLoadPipeline());
        return btnLoad;
    }

    private final JButton makeButtonSavePipeline() {
        JButton btnSave = new JButton("Save pipeline...");
        btnSave.setToolTipText("Save current pipeline tabs for selected image");
        btnSave.addActionListener(ev -> tabHandler.onSavePipeline());
        return btnSave;
    }

    private final JButton makeButtonCancel() {
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(ev -> tabHandler.onCancel());
        return btnCancel;
    }

    private final JButton makeButtonLoadImage() {
        JButton btnLoadImage = new JButton("Load image...");
        btnLoadImage.addActionListener(ev -> onSelectImage());
        if (sourceImage == null)
            SwingUtilities.invokeLater(btnLoadImage::doClick);

        return btnLoadImage;
    }

    private final Component makeButtonUseScale() {
        return makeCheckBox(
            () -> params.useScale,
            v  -> params.useScale = v,
            "Scale",
            "params.useScale", null, null);
    }

    @Override
    protected Component makeUpButtons() {
        Box box4Buttons = Box.createVerticalBox();
        box4Buttons.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        box4Buttons.add(makeButtonLoadImage());
        box4Buttons.add(Box.createVerticalStrut(2));
        box4Buttons.add(makeButtonUseScale());

        return box4Buttons;
    }

    @Override
    protected Component makeDownButtons() {
        Box boxUpButtons = Box.createHorizontalBox();
        boxUpButtons.add(makeButtonLoadPipeline());
        boxUpButtons.add(Box.createHorizontalStrut(6));
        boxUpButtons.add(makeButtonSavePipeline());

        Box boxDownButtons = Box.createHorizontalBox();
        boxDownButtons.add(makeButtonAddFilter());
        boxDownButtons.add(Box.createHorizontalStrut(6));
        boxDownButtons.add(makeButtonCancel());

        Box box4Buttons = Box.createVerticalBox();
        box4Buttons.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        box4Buttons.add(boxUpButtons);
        box4Buttons.add(Box.createVerticalStrut(2));
        box4Buttons.add(boxDownButtons);

        return box4Buttons;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        Container cntrlEditBoxSize = makeEditBox(x -> showImageSize = x, null, "Image size", null, null);
        showImageSize.accept("X*Y");

        box4Options.add(cntrlEditBoxSize);

        return box4Options;
    }

    @Override
    public FirstTabParams getParams() {
        return params;
    }

}
