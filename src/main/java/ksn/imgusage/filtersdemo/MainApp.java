package ksn.imgusage.filtersdemo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.InitLib;
import ksn.imgusage.type.PipelineItem;
import ksn.imgusage.type.dto.FirstTabParams;
import ksn.imgusage.utils.JsonHelper;
import ksn.imgusage.utils.MapperFilter;
import ksn.imgusage.utils.SelectFilterDialog;
import ksn.imgusage.utils.UiHelper;

public class MainApp {

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY  , "TRACE");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY     , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY   , "HH:mm:ss:SSS");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY   , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_LOG_NAME_KEY      , "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
    }

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final String DEFAULT_CAPTION = "Demonstration of image filters";
    private static final File DEFAULT_PIPELINE = Paths.get("exampleImages", "VolodHill.OpenCV.json").toAbsolutePath().toFile();

    private final JFrame frame;
    private JTabbedPane tabPane;
    private BooleanSupplier isScale;
    private List<ITab<?>> tabs = new ArrayList<>();
    private JWindow errorWindow;
    private Timer timer;

    public MainApp() {
        frame = new JFrame(DEFAULT_CAPTION);
        makeLogo();
        initialize();

        // DEBUG: use example pipeline
        SwingUtilities.invokeLater(() -> loadPipeline(DEFAULT_PIPELINE));
    }

    private void initialize() {
        /**/
        // exit by Esc
        Object keyBind = "CloseFrame";
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        frame.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { MainApp.this.onClose(); }
        });
        /**/

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { MainApp.this.onClose(); }
        });

        frame.setResizable(true);
        createComponents();

        frame.pack();
    }

    private void createComponents() {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setBorder(BorderFactory.createEmptyBorder(8,8,2,8));
        tabPane.addChangeListener(this::onTabChanged);

        FirstTab tab = new FirstTab();
        isScale = tab::isScale;
        addTab(tab, null);

        frame.getContentPane().add(tabPane, BorderLayout.CENTER);
    }

    private <TTabParams extends ITabParams> void addTab(ITab<TTabParams> newTab, TTabParams tabParams) {
        ITab<?> prev = null;
        int i = tabPane.getSelectedIndex();
        if (i >= 0)
            prev = tabs.get(i);
        newTab.init(getTabHandler(), prev);
        tabPane.addTab(newTab.getTabName(), newTab.makeTab(tabParams));
        tabPane.setSelectedIndex(i + 1);
        SwingUtilities.invokeLater(tabPane::requestFocus);
        tabs.add(newTab);
    }

    private void onCancel() {
        logger.info("onCancel");
        onClose();
    }
    private void onClose() {
        frame.dispose();
        logger.warn("Good bay!\n\n");
        tabs.forEach(tab -> logger.info("{}.params={}", tab.getClass().getName(), tab.getParams()));
    }

    private void onImageChanged(ITab<?> tab) {
        int pos = tabs.lastIndexOf(tab);
        assert pos > 0;

        tabs.stream().skip(pos + 1).forEach(ITab::resetImage);
    }

    private ITabHandler getTabHandler() {
        return new ITabHandler() {
            @Override public JFrame getFrame()                                         { return MainApp.this.frame; }
            @Override public void onImageChanged(ITab<?> tab)                          {        MainApp.this.onImageChanged(tab); }
            @Override public void onAddNewFilter()                                     {        MainApp.this.onAddNewFilter(); }
            @Override public void onRemoveFilter(ITab<?> tab)                          {        MainApp.this.onRemoveFilter(tab); }
            @Override public void onCancel()                                           {        MainApp.this.onCancel(); }
            @Override public void onImagePanelPaint(JPanel imagePanel, Graphics2D g)   {        MainApp.this.onImagePanelPaint(imagePanel, g); }
            @Override public void onError(String message, ITab<?> tab, Component from) {        MainApp.this.onError(message, from); }
            @Override public void onSavePipeline()                                     {        MainApp.this.onSavePipeline(); }
            @Override public void onLoadPipeline()                                     {        MainApp.this.onLoadPipeline(); }
        };
    }

    private void onAddNewFilter() {
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
                addTab(ctor.newInstance(), params);
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
    }

    private void onRemoveFilter(ITab<?> tab) {
        int pos = tabs.lastIndexOf(tab);
        assert pos > 0;

        tabPane.removeTabAt(pos);
        tabs.remove(tab);
        for (int i = pos; i < tabs.size(); ++i) {
            if (i != 0) {
                ITab<?> prev = tabs.get(i-1);
                ITab<?> curr = tabs.get(i);
                curr.updateSource(prev);
            }
        }
    }

    private void onImagePanelPaint(JPanel imagePanel, Graphics2D g) {
        logger.trace("onImagePanelPaint");

        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0xAE, 0xD6, 0xF1));
        g.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());

        int i = tabPane.getSelectedIndex();
        if (i < 0)
            return;
        if (i >= tabs.size())
            return;

        ITab<?> tab = tabs.get(i);
        BufferedImage image = tab.getImage();
        if (image == null)
            return;

        if (i == 0)
            image = ((FirstTab)tab).getPreviewImage();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        logger.trace("image.size={{}, {}}", image     .getWidth(), image     .getHeight());
        logger.trace("panel.size={{}, {}}", imagePanel.getWidth(), imagePanel.getHeight());

        if (isScale.getAsBoolean()) {
            double zoomX = imagePanel.getWidth()  / (double)image.getWidth();
            double zoomY = imagePanel.getHeight() / (double)image.getHeight();
            double zoom = Math.min(zoomX, zoomY);
            logger.trace("zoom={}", zoom);
            g.drawImage(image, 0,0, (int)(zoom * image.getWidth()), (int)(zoom * image.getHeight()), (ImageObserver)null);
        } else {
            g.drawImage(image, 0,0, (ImageObserver)null);
        }

    }

    void onTabChanged(ChangeEvent ev) {
        logger.trace("onTabChanged");
    }

    private void onError(String message, Component from) {
        if (from == null)
            from = frame.getRootPane();

        if (errorWindow == null) {
            JLabel errorLabel = new JLabel(message);
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
        File latestDir = ((FirstTab)tabs.get(0)).getLatestImageDir();
        File jsonFile = UiHelper.saveFiltersPipelineFile(frame, latestDir);
        if (jsonFile == null)
            return; // aborted

        List<PipelineItem> pipeline = new ArrayList<>(tabs.size());
        for (int i = 0; i < tabs.size(); ++i) {
            PipelineItem item = new PipelineItem();
            item.pos = i;
            item.tabName = tabs.get(i).getTabFullName();
            item.params  = tabs.get(i).getParams();
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
            onError("Can`t convert to JSON: " + ex, frame);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            logger.info("Pipeline saved to file {}", jsonFile);
        } catch (Exception ex) {
            logger.error("Can`t save file '{}': {}", jsonFile, ex);
            onError("Can`t save file '" + jsonFile + "': " + ex, frame);
        } finally {
            firstParams.imageFile = tmp;
        }
    }

    private void onLoadPipeline() {
        File latestDir = ((FirstTab)tabs.get(0)).getLatestImageDir();
        File jsonFile = UiHelper.loadFiltersPipelineFile(frame, latestDir);
        if (jsonFile == null)
            return; // aborted
        loadPipeline(jsonFile);
    }

    private void loadPipeline(File jsonFile) {
        if (!jsonFile.exists()) {
            logger.error("File not found: {}", jsonFile);
            return;
        }

        List<PipelineItem> pipeline;
        try (FileInputStream fis = new FileInputStream(jsonFile)) {
            pipeline = JsonHelper.fromJson(fis, new TypeReference<List<PipelineItem>>() {});
            logger.info("Pipeline loaded from file {}", jsonFile);
        } catch (Exception ex) {
            logger.error("Can`t convert to JSON from file {}: {}", jsonFile, ex);
            onError("Can`t convert to JSON from file '" + jsonFile + "' : " + ex, frame);
            return;
        }

        // restore full path from relative
        FirstTabParams firstParams = (FirstTabParams)pipeline.get(0).params;
        firstParams.imageFile = jsonFile.toPath().getParent().resolve(firstParams.imageFile.toPath()).toFile();

        // remove all
        do {
            onRemoveFilter(tabs.get(0));
        } while(!tabs.isEmpty());

        pipeline.sort((p1, p2) -> {
            if (p1.pos > p2.pos) return  1;
            if (p1.pos < p2.pos) return -1;
            return 0;
        });

        for (PipelineItem item : pipeline)
            addTabByFilterFullName(item.tabName, item.params);
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
                MainApp mainWin = new MainApp();
                mainWin.frame.setVisible(true);
            });
        } catch (Exception ex) {
            logger.error(MainApp.class.getSimpleName() + "::main", ex);
        }
    }

}
