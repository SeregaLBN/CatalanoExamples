package ksn.imgusage.type;

public class Point {

    public int x;
    public int y;

    public Point() {}

    public Point(int width, int height) {
        this.x = width;
        this.y = height;
    }

    @Override
    public String toString() {
        return "{ x=" + x + ", y=" + y + " }";
    }

}
