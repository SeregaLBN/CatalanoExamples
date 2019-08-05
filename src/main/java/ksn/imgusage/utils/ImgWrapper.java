package ksn.imgusage.utils;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

import Catalano.Imaging.FastBitmap;

public class ImgWrapper {

    private final BufferedImage img;
    private final FastBitmap    bmp;
    private final Mat           mat;

    public ImgWrapper(BufferedImage img) {
        if (img == null)
            throw new IllegalArgumentException();
        this.img = img;
        this.bmp = null;
        this.mat = null;
    }

    public ImgWrapper(FastBitmap bmp) {
        if (bmp == null)
            throw new IllegalArgumentException();
        this.img = null;
        this.bmp = bmp;
        this.mat = null;
    }

    public ImgWrapper(Mat mat) {
        if (mat == null)
            throw new IllegalArgumentException();
        this.img = null;
        this.bmp = null;
        this.mat = mat;
    }

    public BufferedImage getBufferedImage() {
        if (img != null)
            return img;
        if (bmp != null)
            return bmp.toBufferedImage();
        return OpenCvHelper.toImage(mat);
    }

    public FastBitmap getFastBitmap() {
        if (bmp != null)
            return bmp;
        if (img != null)
            return new FastBitmap(img);
        return new FastBitmap(OpenCvHelper.toImage(mat));
    }

    public Mat getMat() {
        if (mat != null)
            return mat;
        if (bmp != null)
            return OpenCvHelper.fromImage(bmp.toBufferedImage());
        return OpenCvHelper.fromImage(img);
    }

}
