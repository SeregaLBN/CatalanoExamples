package ksn.imgusage.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFileChooser;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;

public final class UiHelper {
    private UiHelper() {}

    public static File selectImageFile(File oldFile) {
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

    public static void debounceExecutor(Supplier<Timer> getterTimer, Consumer<Timer> setterTimer, int debounceTimer, Runnable executor, Logger logger) {
        Timer timer = getterTimer.get();
        if (timer == null) {
            Timer[] wrapper = { null };
            wrapper[0] = timer = new Timer(debounceTimer, ev -> {
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

}
