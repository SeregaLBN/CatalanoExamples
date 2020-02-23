package ksn.imgusage.tabs.opencv;

public class InitLib {
    private InitLib() {}

    static {
        new org.bytedeco.opencv.opencv_java();
//        nu.pattern.OpenCV.loadLocally();
    }

    public static void loadOpenCV() {
        // implicit call static block
    }

}
