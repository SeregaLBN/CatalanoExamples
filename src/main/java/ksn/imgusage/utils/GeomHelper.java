package ksn.imgusage.utils;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public final class GeomHelper {
    private GeomHelper() {}

    public static boolean isIntersected(Rect rc1, Rect rc2) {
        Point br1 = rc1.br();
        Point br2 = rc2.br();
        int x = Math.max(rc1.x, rc2.x);
        int y = Math.max(rc1.y, rc2.y);
        double l = Math.min(br1.x, br2.x);
        double b = Math.min(br1.y, br2.y);
        return (x < l)
            && (y < b);
    }

    /** @see equals {@link java.awt.geom.Area#intersect} */
    public static Rect intersect(Rect rc1, Rect rc2) {
        Point br1 = rc1.br();
        Point br2 = rc2.br();
        return new Rect(
            new Point(Math.max(rc1.x, rc2.x),
                      Math.max(rc1.y, rc2.y)),
            new Point(Math.min(br1.x, br2.x),
                      Math.min(br1.y, br2.y))
        );
    }

    /** @see equals {@link java.awt.geom.Area#add} */
    public static Rect intersectInclude(Rect rc1, Rect rc2) {
        Point br1 = rc1.br();
        Point br2 = rc2.br();
        return new Rect(
            new Point(Math.min(rc1.x, rc2.x),
                      Math.min(rc1.y, rc2.y)),
            new Point(Math.max(br1.x, br2.x),
                      Math.max(br1.y, br2.y))
        );
    }

}
