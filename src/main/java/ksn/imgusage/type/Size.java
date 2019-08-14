package ksn.imgusage.type;

public class Size {

    public int width;
    public int height;

    public Size() {}

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "{ width=" + width + ", height=" + height + " }";
    }

}
