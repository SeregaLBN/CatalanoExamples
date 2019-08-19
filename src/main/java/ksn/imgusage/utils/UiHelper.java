package ksn.imgusage.utils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;

public final class UiHelper {
    private UiHelper() {}

    private static File chooseFileToLoad(Component parent, File currentDir, FileFilter filter) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (currentDir != null)
            fileChooser.setCurrentDirectory(currentDir);

        int option = fileChooser.showOpenDialog(parent);
        if (option == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();

        return null;
    }

    private static File chooseFileToSave(Component parent, File currentDir, FileFilter filter) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (currentDir != null)
            fileChooser.setCurrentDirectory(currentDir);

        int option = fileChooser.showSaveDialog(parent);
        if (option == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();

        return null;
    }

    public static File chooseFileToLoadImage(Component parent, File currentDir) {
        return chooseFileToLoad(parent, currentDir, new ImageFilter());
    }

    public static File chooseFileToSavePngImage(Component parent, File currentDir) {
        return chooseFileToSave(parent, currentDir, new ImageOnlyPngFilter());
    }

    public static File chooseFileToLoadPipeline(Component parent, File currentDir) {
        return chooseFileToLoad(parent, currentDir, new PipelineFilter());
    }

    public static File chooseFileToSavePipeline(Component parent, File currentDir) {
        return chooseFileToSave(parent, currentDir, new PipelineFilter());
    }

    private static class InternalFilter extends FileFilter {

        private final List<String> extensions;
        private final String description;

        protected InternalFilter(List<String> extensions, String description) {
            this.extensions = extensions;
            this.description = description;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory())
                return true;
            String fileName = file.getName();
            int pos = fileName.lastIndexOf('.');
            if (pos < 0)
                return false;
            String ext = fileName.substring(pos + 1);
            return extensions.stream().anyMatch(ext::equalsIgnoreCase);
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    private static class ImageFilter extends InternalFilter {
        public ImageFilter() {
            super(Arrays.asList("jpeg", "jpg", "gif", "tiff", "tif", "png"), "Image Only");
        }
    }
    private static class PipelineFilter extends InternalFilter {
        public PipelineFilter() {
            super(Arrays.asList("json"),  "Pipeline filters (*.json)");
        }
    }
    private static class ImageOnlyPngFilter extends InternalFilter {
        public ImageOnlyPngFilter() {
            super(Arrays.asList("png"), "PNG Only");
        }
    }

    public static void debounceExecutor(Supplier<Timer> getterTimer, Consumer<Timer> setterTimer, int debounceTime, Runnable executor, Logger logger) {
        Timer timer = getterTimer.get();
        if (timer == null) {
            Timer[] wrapper = { null };
            wrapper[0] = timer = new Timer(debounceTime, ev -> {
                wrapper[0].stop();
                logger.trace("debounce: call executor...");
                executor.run();
            });
            setterTimer.accept(timer);
        }

        if (timer.isRunning())
            timer.restart();
        else
            timer.start();
    }

    public static void bindKey(JRootPane rootPane, KeyStroke keyCombo, Runnable action) {
        Object keyBind = UUID.randomUUID();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyCombo, keyBind);
        rootPane.getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

}
