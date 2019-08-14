package ksn.imgusage.utils;

public class Padding {

    public int left;
    public int top;
    public int right;
    public int bottom;

    public Padding(int left, int top, int right, int bottom) {
        this.left   = left;
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "{ left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + " }";
    }

}
