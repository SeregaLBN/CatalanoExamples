package ksn.imgusage.filtersdemo;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.ws.rs.core.GenericType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabManager;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.InitLib;
import ksn.imgusage.tabs.opencv.PerspectiveTransformTab;
import ksn.imgusage.type.PipelineItem;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.utils.JsonHelper;
import ksn.imgusage.utils.MapperFilter;
import ksn.imgusage.utils.SelectFilterDialog;
import ksn.imgusage.utils.UiHelper;

public class ImageFilterExamples {

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY  , "TRACE");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY     , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY   , "HH:mm:ss:SSS");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY   , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_LOG_NAME_KEY      , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageFilterExamples.class);
    public static final String DEFAULT_TITLE = "Demonstration of image filters";
    private static final File DEFAULT_PIPELINE = Path.of("exampleImages", "Lena.OpenCV.Neon.json").toAbsolutePath().toFile();

    private final JFrame frame;
    private JTabbedPane tabPane;
    private BooleanSupplier isScale;
    private List<ITab<?>> tabs = new ArrayList<>();
    private JWindow errorWindow;
    private Timer timer;

    public ImageFilterExamples() {
        frame = new JFrame(DEFAULT_TITLE);
        AppInfo.setRootFrame(frame);
        makeLogo();
        initialize();

        // DEBUG: use example pipeline
        SwingUtilities.invokeLater(() -> loadPipeline(DEFAULT_PIPELINE));
    }

    private void initialize() {
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_EXIT_APP           .keyCode, UiHelper.KEY_COMBO_EXIT_APP           .modifiers, false), this::onClose); // exit by Esc
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_ADD_NEW_FILTER1    .keyCode, UiHelper.KEY_COMBO_ADD_NEW_FILTER1    .modifiers, false), this::onAddNewTab);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_ADD_NEW_FILTER2    .keyCode, UiHelper.KEY_COMBO_ADD_NEW_FILTER2    .modifiers, false), this::onAddNewTab);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_ADD_NEW_FILTER3    .keyCode, UiHelper.KEY_COMBO_ADD_NEW_FILTER3    .modifiers, false), this::onAddNewTab);
      //UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_DEL_CURRENT_FILTER1.keyCode, UiHelper.KEY_COMBO_DEL_CURRENT_FILTER1.modifiers, false), this::onRemoveCurrentFilter);
      //UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_DEL_CURRENT_FILTER2.keyCode, UiHelper.KEY_COMBO_DEL_CURRENT_FILTER2.modifiers, false), this::onRemoveCurrentFilter);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_DEL_CURRENT_FILTER3.keyCode, UiHelper.KEY_COMBO_DEL_CURRENT_FILTER3.modifiers, false), this::onRemoveCurrentTab);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_DEL_CURRENT_FILTER4.keyCode, UiHelper.KEY_COMBO_DEL_CURRENT_FILTER4.modifiers, false), this::onRemoveCurrentTab);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_DEL_ALL_FITERS     .keyCode, UiHelper.KEY_COMBO_DEL_ALL_FITERS     .modifiers, false), this::onRemoveAllFilters);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_LOAD_PIPELINE      .keyCode, UiHelper.KEY_COMBO_LOAD_PIPELINE      .modifiers, false), this::onLoadPipeline);
        UiHelper.bindKey(frame.getRootPane(), KeyStroke.getKeyStroke(UiHelper.KEY_COMBO_OPEN_IMAGE_OR_VIDEO.keyCode, UiHelper.KEY_COMBO_OPEN_IMAGE_OR_VIDEO.modifiers, false), this::onSelectImageOrVideo);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { ImageFilterExamples.this.onClose(); }
        });

        frame.setResizable(true);
        createComponents();

        frame.pack();
    }

    private void createComponents() {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(8,8,2,8));
        tabPane.addChangeListener(this::onTabChanged);

        addTab(new FirstTab(), null);
        initFirstTab();

        frame.getContentPane().add(tabPane, BorderLayout.CENTER);
    }

    private void initFirstTab() {
        FirstTab tab = getFirstTab();
        tab.setSavePipelineHandler(this::onSavePipeline);
        tab.setLoadPipelineHandler(this::onLoadPipeline);
        isScale = tab::isScale;
    }

    private <TTabParams extends ITabParams> void addTab(ITab<TTabParams> newTab, TTabParams tabParams) {
        final int newPos = tabPane.getSelectedIndex() + 1;
        newTab.setManager(getTabManager());

        tabPane.insertTab(
                newTab.getTitle(),
                null,
                newTab.makeTab(tabParams),
                ((newTab.getGroup() == null)
                        ? ""
                        : (newTab.getGroup() + ": "))
                    + newTab.getDescription(),
                newPos);
        tabs.add(newPos, newTab);

        tabPane.setSelectedIndex(newPos);
        SwingUtilities.invokeLater(tabPane::requestFocus);
    }

    private void onCancel() {
        logger.info("onCancel");
        onClose();
    }

    private void onClose() {
        tabs.forEach(ITab::close);
        frame.dispose();
        logger.info("Good bay!\n\n");
        tabs.forEach(tab -> logger.info("{}.params={}", tab.getClass().getName(), tab.getParams()));
    }


    private ITabManager getTabManager() {
        return new ITabManager() {
            @Override public ITab<?> getPrevTab(ITab<?> self)                                { return ImageFilterExamples.this.getPrevTab(self); }
            @Override public ITab<?> getNextTab(ITab<?> self)                                { return ImageFilterExamples.this.getNextTab(self); }
            @Override public void    onAddNewFilter()                                        {        ImageFilterExamples.this.onAddNewTab(); }
            @Override public void    onRemoveFilter(ITab<?> tab)                             {        ImageFilterExamples.this.onRemoveTab(tab); }
            @Override public void    onCancel()                                              {        ImageFilterExamples.this.onCancel(); }
            @Override public void    onImgPanelDraw(JPanel imgPanel, Graphics2D g, Logger l) {        ImageFilterExamples.this.onImgPanelDraw(imgPanel, g, l); }
            @Override public void    onError(Exception ex, ITab<?> tab, Component from)      {        ImageFilterExamples.this.onError(ex, tab, from); }
        };
    }

    private ITab<?> getPrevTab(ITab<?> self) {
        int pos = tabs.indexOf(self);
        if (pos < 1)
            return null;
        return tabs.get(pos - 1);
    }

    private ITab<?> getNextTab(ITab<?> self) {
        int pos = tabs.indexOf(self);
        if (pos < 0)
            return null;
        if (pos >= tabs.size() -1)
            return null;
        return tabs.get(pos + 1);
    }

    private void onAddNewTab() {
        logger.trace("onAddNewFilter");
        String filterTabFullName = new SelectFilterDialog(frame).getFilterTabFullName();
        if (filterTabFullName != null)
            addTabByFilterFullName(filterTabFullName, null);
    }

    private <TTabParams extends ITabParams> void addTabByFilterFullName(String filterTabFullName, TTabParams params) {
        Class<? extends ITab<?>> tabClass = MapperFilter.getTabClass(filterTabFullName);
        if (tabClass == null)
            logger.error("Not supported filter {}", filterTabFullName);
        else
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends ITab<TTabParams>> ctor = (Constructor<? extends ITab<TTabParams>>)tabClass.getConstructor();
                ITab<TTabParams> newTab = ctor.newInstance();
                if (newTab instanceof PerspectiveTransformTab)
                    ((PerspectiveTransformTab)newTab).setFirstTabSupplier(this::getFirstTab);
                addTab(newTab, params);
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
    }

    private void onRemoveCurrentTab() {
        int i = tabPane.getSelectedIndex();
        if (i > 0)
            onRemoveTab(tabs.get(i));
    }

    private void onRemoveAllFilters() {
        while (tabs.size() > 1)
            onRemoveTab(tabs.get(tabs.size() - 1));
    }

    private void onRemoveTab(ITab<?> tab) {
        ITab<?> nextTab = getNextTab(tab);

        tab.close();

        int pos = tabs.indexOf(tab);
        assert pos >= 0;

        tabPane.removeTabAt(pos);
        tabs.remove(tab);

        if (nextTab != null)
            nextTab.invalidate();
    }

    private void onImgPanelDraw(JPanel imagePanel, Graphics2D g, Logger l) {
        l.trace("onImgPanelDraw");

        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0xAE, 0xD6, 0xF1));
        g.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());

        int i = tabPane.getSelectedIndex();
        if (i < 0)
            return;
        if (i >= tabs.size())
            return;

        ITab<?> tab = tabs.get(i);
        BufferedImage image = tab.getDrawImage();
        if (image == null)
            return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        l.trace("image.size={{}, {}}", image     .getWidth(), image     .getHeight());
//        l.trace("panel.size={{}, {}}", imagePanel.getWidth(), imagePanel.getHeight());

        if (isScale.getAsBoolean()) {
            double zoomX = imagePanel.getWidth()  / (double)image.getWidth();
            double zoomY = imagePanel.getHeight() / (double)image.getHeight();
            double zoom = Math.min(zoomX, zoomY);
//            l.trace("zoom={}", zoom);
            g.drawImage(image, 0,0, (int)(zoom * image.getWidth()), (int)(zoom * image.getHeight()), (ImageObserver)null);
        } else {
            g.drawImage(image, 0,0, (ImageObserver)null);
        }

    }

    private void onTabChanged(ChangeEvent ev) {
        logger.trace("onTabChanged");
    }

    private void onError(Exception ex, ITab<?> tab, Component from) {
        if (from == null)
            from = frame.getRootPane();

        if (errorWindow == null) {
            String errMsg = (ex.getMessage() == null)
                ? ex.getClass().getSimpleName()
                : ex.toString();
            JLabel errorLabel = new JLabel(
                (tab == null
                    ? ""
                    : tab.getClass().getSimpleName() + ": ")
                + errMsg);
            errorWindow = new JWindow(frame);
            JPanel contentPane = (JPanel) errorWindow.getContentPane();
            contentPane.add(errorLabel);
            contentPane.setBackground(new Color(0xFA, 0xC5, 0xAF));
            contentPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            errorWindow.pack();
        }

        if (from.isShowing()) {
            Point loc = from.getLocationOnScreen();
            errorWindow.setLocation(loc.x + 20, loc.y + 30);
        }
        errorWindow.setVisible(true);

        UiHelper.debounceExecutor(() -> timer, t -> timer = t, 5000, () -> errorWindow.setVisible(false), logger);
    }

    private void makeLogo() {
        final int w = 128;
        final int h = 128;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();
        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.WHITE);
            int border = 10;
            g.fillRect(border, border, w-2*border, h-2*border);

            g.setColor(Color.ORANGE);
            g.fillOval(20, 20, 40, 40);

            g.setColor(Color.BLACK);
            Path2D.Double triangleShape = new Path2D.Double();
            triangleShape.moveTo( 20, h - 20);
            triangleShape.lineTo( 50, h - 20 - 30);
            triangleShape.lineTo(100, h - 20);
            triangleShape.closePath();
            g.fill(triangleShape);

            g.setColor(Color.DARK_GRAY);
            triangleShape = new Path2D.Double();
            triangleShape.moveTo(w - 80, h - 20);
            triangleShape.lineTo(w - 40, h - 20 - 70);
            triangleShape.lineTo(w - 20, h - 20);
            triangleShape.closePath();
            g.fill(triangleShape);
        } finally {
            g.dispose();
        }

        frame.setIconImage(img);
    }

    private void onSavePipeline() {
        File jsonFile = UiHelper.chooseFileToSavePipeline(frame, AppInfo.getLatestPipelineDir());
        if (jsonFile == null)
            return; // aborted

        jsonFile = SelectFilterDialog.checkExtension(jsonFile, "json");

        List<PipelineItem> pipeline = new ArrayList<>(tabs.size());
        for (ITab<?> tab : tabs) {
            PipelineItem item = new PipelineItem();
            item.tabName = tab.getName();
            item.params  = tab.getParams();
            pipeline.add(item);
        }

        // cast absolute image-file path to relative from json-pipeline-file
        FirstTabParams firstParams = (FirstTabParams)pipeline.get(0).params;
        File tmp = firstParams.imageFile;
        firstParams.imageFile = SelectFilterDialog.getRelativePath(firstParams.imageFile, jsonFile.getParentFile());


        String json;
        try {
            json = JsonHelper.toJson(pipeline, true);
        } catch (Exception ex) {
            logger.error("Can`t convert to JSON: {}", ex);
            onError(new Exception("Can`t convert to JSON", ex), null, frame);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            logger.info("Pipeline saved to file {}", jsonFile);
            AppInfo.setLatestPipelineDir(jsonFile.getParentFile().toPath());
        } catch (Exception ex) {
            logger.error("Can`t save file '{}': {}", jsonFile, ex);
            onError(new Exception("Can`t save file '" + jsonFile + "'", ex), null, frame);
        } finally {
            firstParams.imageFile = tmp;
        }
    }

    private void onLoadPipeline() {
        File jsonFile = UiHelper.chooseFileToLoadPipeline(frame, AppInfo.getLatestPipelineDir());
        if (jsonFile == null)
            return; // aborted

        loadPipeline(jsonFile);
    }

    private void onSelectImageOrVideo() {
        getFirstTab().onSelectImageOrVideo();
    }

    private void loadPipeline(File jsonFile) {
        if (!jsonFile.exists()) {
            logger.error("File not found: {}", jsonFile);
            return;
        }

        List<PipelineItem> pipeline;
        try (FileInputStream fis = new FileInputStream(jsonFile)) {
            String json = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            pipeline = JsonHelper.fromJson(json, new GenericType<List<PipelineItem>>() {});
            logger.info("Pipeline loaded from file {}", jsonFile);
        } catch (Exception ex) {
            logger.error("Can`t convert to JSON from file {}: {}", jsonFile, ex);
            onError(new Exception("Can`t convert to JSON from file '" + jsonFile + "'", ex), null, frame);
            return;
        }

        // restore full path from relative
        FirstTabParams firstParams = (FirstTabParams)pipeline.get(0).params;
        firstParams.imageFile = jsonFile.toPath().getParent().resolve(firstParams.imageFile.toPath()).toFile();

        if (!FirstTab.TAB_NAME.equals(pipeline.get(0).tabName)) {
            logger.error("FirstTab mus be instanceof {}", FirstTab.class.getName());
            onError(new Exception("FirstTab mus be instanceof " + FirstTab.class.getName()), null, frame);
        }

        onRemoveAllFilters();
        onRemoveTab(tabs.get(0));

        for (PipelineItem item : pipeline)
            addTabByFilterFullName(item.tabName, item.params);

        initFirstTab();

        frame.setTitle(frame.getTitle() +": pipline " + jsonFile.getName());

        AppInfo.setLatestPipelineDir(jsonFile.getParentFile().toPath());
    }

    private FirstTab getFirstTab() {
        return (FirstTab)tabs.get(0);
    }

    public static void main(String[] args) {
        try {
            InitLib.loadOpenCV();
        } catch (Exception ex) {
            logger.error("Can not load openCV library", ex);
            System.exit(2);
            return;
        }

        try {
            UIManager.put("Slider.paintValue", false);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.warn("", ex);
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                ImageFilterExamples mainWin = new ImageFilterExamples();
                mainWin.frame.setVisible(true);
            });
        } catch (Exception ex) {
            logger.error(ImageFilterExamples.class.getSimpleName() + "::main", ex);
        }
    }

}
