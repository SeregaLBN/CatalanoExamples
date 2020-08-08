package ksn.imgusage.filtersdemo;

import java.nio.file.Path;

import javax.swing.JFrame;

/** Shared info */
public final class AppInfo {
    private AppInfo() {}

    private static JFrame rootFrame;

    private static Path latestImageDir;
    private static Path latestVideoDir;
    private static Path latestPipelineDir;

    public static JFrame getRootFrame()                 { return rootFrame; }
    public static void  setRootFrame(JFrame rootFrame) { AppInfo.rootFrame = rootFrame; }

    public static Path getLatestImageDir()                     { return latestImageDir; }
    public static void setLatestImageDir(Path latestImageDir) { AppInfo.latestImageDir = latestImageDir;}

    public static Path getLatestVideoDir()                     { return latestVideoDir; }
    public static void setLatestVideoDir(Path latestVideoDir) { AppInfo.latestVideoDir = latestVideoDir; }

    public static Path getLatestPipelineDir()                        { return latestPipelineDir; }
    public static void setLatestPipelineDir(Path latestPipelineDir) { AppInfo.latestPipelineDir = latestPipelineDir; }

}
