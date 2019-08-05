package ksn.imgusage.tabs.opencv;

public class InitLib {
    private InitLib() {}

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static void loadOpenCV() {
        // implicit call static block
    }

}
