package ksn.imgusage.filtersdemo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.opencv.core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.Filters.*;
import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.tabs.ITabHandler;
import ksn.imgusage.tabs.catalano.*;
import ksn.imgusage.tabs.opencv.AsIsTab;
import ksn.imgusage.tabs.opencv.GaussianBlurTab;
import ksn.imgusage.tabs.opencv.InitLib;
import ksn.imgusage.tabs.opencv.MorphologyExTab;
import ksn.imgusage.tabs.opencv.type.CvBorderTypes;
import ksn.imgusage.tabs.opencv.type.CvMorphShapes;
import ksn.imgusage.tabs.opencv.type.CvMorphTypes;
import ksn.imgusage.tabs.opencv.type.IMatter;
import ksn.imgusage.utils.SelectFilterDialog;

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

    private final JFrame frame;
    private JTabbedPane tabPane;
    private BooleanSupplier isScale;
    private List<ITab> tabs = new ArrayList<>();
    private boolean useExamplePipeline = true;

    public MainApp() {
        frame = new JFrame(DEFAULT_CAPTION);
        initialize();

        // DEBUG
        if (useExamplePipeline)
            examplePipeline();
    }

    private void initialize() {
        /**/
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

        if (!useExamplePipeline) {
            FirstTab tab = new FirstTab(getTabHandler());
            isScale = tab::isScale;
            tabs.add(tab);
        }

        frame.getContentPane().add(tabPane, BorderLayout.CENTER);
    }

    private JTabbedPane getTabPanel() {
        return tabPane;
    }

    private void onCancel() {
        logger.info("onCancel");
        onClose();
    }
    private void onClose() {
        frame.dispose();
        logger.warn("Good bay!\n\n");
        tabs.forEach(ITab::printParams);
    }

    private void onImageChanged(ITab tab) {
        int pos = tabs.lastIndexOf(tab);
        assert pos > 0;

        tabs.stream().skip(pos + 1).forEach(ITab::resetImage);
    }

    private ITabHandler getTabHandler() {
        return new ITabHandler() {
            @Override
            public JTabbedPane getTabPanel() { return MainApp.this.getTabPanel(); }
            @Override
            public void onImageChanged(ITab tab) {    MainApp.this.onImageChanged(tab); }
            @Override
            public void onAddNewFilter() {            MainApp.this.onAddNewFilter(); }
            @Override
            public void onRemoveFilter(ITab tab) {    MainApp.this.onRemoveFilter(tab); }
            @Override
            public void onCancel() {                  MainApp.this.onCancel(); }
            @Override
            public void onImagePanelPaint(JPanel imagePanel, Graphics2D g) {
                                                      MainApp.this.onImagePanelPaint(imagePanel, g);
            }
        };
    }

    private void onAddNewFilter() {
        logger.trace("onAddNewFilter");
        String filterClassName = new SelectFilterDialog(frame).getFilterClassName();
        if (filterClassName == null)
            return;

        ITab lastTab = tabs.get(tabs.size() - 1);
        Function<Class<? extends ITab>, ITab> callCtor = tabClass -> {
            try {
                Constructor<? extends ITab> ctor = tabClass.getConstructor(ITabHandler.class, ITab.class);
                return ctor.newInstance(getTabHandler(), lastTab);
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
            return null;
        };
        BiFunction<String /* filterName */, Class<? extends ITab>, ITab> opencvHandler = (filterName, tabClass) -> {
            if (filterClassName.equals(SelectFilterDialog.OPENCV_TAB_PREFIX + filterName))
                return callCtor.apply(tabClass);
            return null;
        };
        BiFunction<Class<?> /* Catalano filter class */, Class<? extends ITab>, ITab> catalanoHandler = (filterClass, tabClass) -> {
            if (filterClassName.equals(SelectFilterDialog.CATALANO_TAB_PREFIX + filterClass.getSimpleName()))
                return callCtor.apply(tabClass);
            return null;
        };

        // map OpenCV filters to tab classes
        Stream<Supplier<ITab>> opencvMapping = Stream.of( // alphabetical sort
            () -> opencvHandler.apply("AsIs"        , AsIsTab        .class),
            () -> opencvHandler.apply("GaussianBlur", GaussianBlurTab.class),
            () -> opencvHandler.apply("MorphologyEx", MorphologyExTab.class)
        );

        // map Catalano filters to tab classes
        Stream<Supplier<ITab>> catalanoMapping = Stream.of( // alphabetical sort
            () -> catalanoHandler.apply(AdaptiveContrastEnhancement.class,      AdaptiveContrastTab.class),
            () -> catalanoHandler.apply(ArtifactsRemoval           .class,      ArtifactsRemovalTab.class),
            () -> catalanoHandler.apply(BernsenThreshold           .class,      BernsenThresholdTab.class),
            () -> catalanoHandler.apply(Blur                       .class,                  BlurTab.class),
            () -> catalanoHandler.apply(BradleyLocalThreshold      .class, BradleyLocalThresholdTab.class),
            () -> catalanoHandler.apply(BrightnessCorrection       .class,  BrightnessCorrectionTab.class),
            () -> catalanoHandler.apply(FrequencyFilter            .class,       FrequencyFilterTab.class),
            () -> catalanoHandler.apply(Rotate                     .class,                RotateTab.class)
        );

        ITab newTab = Stream.concat(opencvMapping, catalanoMapping)
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
        if (newTab == null)
            logger.error("Not supported filter {}", filterClassName);
        else
            tabs.add(newTab);
    }

    private void onRemoveFilter(ITab tab) {
        int pos = tabs.lastIndexOf(tab);
        assert pos > 0;

        tabPane.removeTabAt(pos);
        tabs.remove(tab);
        for (int i = pos; i < tabs.size(); ++i) {
            ITab prev = tabs.get(i-1);
            ITab curr = tabs.get(i);
            curr.updateSource(prev);
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

        BufferedImage image = tabs.get(i).getImage();
        if (image == null)
            return;

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

    private void examplePipeline() {
        FirstTab firstTab = new FirstTab(getTabHandler(), FirstTab.DEFAULT_IMAGE, false, true);
        isScale = firstTab::isScale;
        tabs.add(firstTab);

        if (firstTab.getImage() != null) {
            ITab prevTab = firstTab;

          //prevTab = examplePipelineCatalanoFilters(prevTab);
            prevTab = examplePipelineOpenCvFilters  (prevTab);
        }

        frame.pack();
    }

    private ITab examplePipelineCatalanoFilters(ITab prevTab) {
        List<UnaryOperator<ITab>> nextTabs = Arrays.asList(
            // supported full colors
            prevTab2 -> new  BrightnessCorrectionTab(getTabHandler(), prevTab2, true, 1),
            prevTab2 -> new                  BlurTab(getTabHandler(), prevTab2, true),
            prevTab2 -> new                RotateTab(getTabHandler(), prevTab2, true, 0.01, true, Rotate.Algorithm.BICUBIC),

            // only grayscale
            prevTab2 -> new       FrequencyFilterTab(getTabHandler(), prevTab2, true, 0, 200),
            prevTab2 -> new      AdaptiveContrastTab(getTabHandler(), prevTab2, true, 4, 0.84, 0.02, 2.4, 4.93),
            prevTab2 -> new      BernsenThresholdTab(getTabHandler(), prevTab2, true, 6, 30),
            prevTab2 -> new BradleyLocalThresholdTab(getTabHandler(), prevTab2, true, 10, 70),
            prevTab2 -> new      ArtifactsRemovalTab(getTabHandler(), prevTab2, true, 9)
        );
        for (UnaryOperator<ITab> fTab : nextTabs) {
            ITab next = fTab.apply(prevTab);
            tabs.add(next);
            prevTab = next;
        }
        return prevTab;
    }

    private ITab examplePipelineOpenCvFilters(ITab prevTab) {
        List<UnaryOperator<ITab>> nextTabs = Arrays.asList(
          //prevTab2 -> new         AsIsTab(getTabHandler(), prevTab2, null, false),
            prevTab2 -> new GaussianBlurTab(getTabHandler(), prevTab2, null, new Size(5, 5), 15, 15, CvBorderTypes.BORDER_DEFAULT),
            prevTab2 -> new MorphologyExTab(getTabHandler(), prevTab2, null, CvMorphTypes.MORPH_CLOSE, new IMatter.StructuringElementParams(CvMorphShapes.MORPH_ELLIPSE, 7,7, -1,-1))
        );
        for (UnaryOperator<ITab> fTab : nextTabs) {
            ITab next = fTab.apply(prevTab);
            tabs.add(next);
            prevTab = next;
        }
        return prevTab;
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
