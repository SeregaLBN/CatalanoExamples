package ksn.imgusage.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Resize;

public final class ImgHelper {
    private ImgHelper() {}


    public static Mat copy(Mat from) {
        return from.clone();
    }
    public static FastBitmap copy(FastBitmap from) {
        return new FastBitmap(from);
    }
    public static BufferedImage copy(BufferedImage from) {
        ColorModel cm = from.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = from.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage toBufferedImage(FastBitmap from) {
        return from.toBufferedImage();
    }
    public static BufferedImage toBufferedImage(Mat from) {
        return OpenCvHelper.toImage(from);
    }

    public static FastBitmap toFastBitmap(BufferedImage from) {
        return new FastBitmap(from);
    }
    public static FastBitmap toFastBitmap(Mat from) {
        return new FastBitmap(OpenCvHelper.toImage(from));
    }

    public static Mat toMat(BufferedImage from) {
        return OpenCvHelper.fromImage(from);
    }
    public static Mat toMat(FastBitmap from) {
        return OpenCvHelper.fromImage(from.toBufferedImage());
    }

    public static Mat resize(Mat from, int newWidth, int newHeight) {
        Mat resizeimage = new Mat();
        Imgproc.resize(from, resizeimage, new Size(newWidth, newHeight));
        return resizeimage;
    }
    public static FastBitmap resize(FastBitmap from, int newWidth, int newHeight) {
        return new Resize(newWidth, newHeight).apply(from);
    }
    public static BufferedImage resize(BufferedImage from, int newWidth, int newHeight) {
        Image tmp = from.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        if (tmp instanceof BufferedImage)
            return (BufferedImage)tmp;

        BufferedImage dimg = new BufferedImage(newWidth, newHeight, from.getType());
        Graphics2D g = dimg.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
        return dimg;
    }

    public static Color toGrayscale(Color c) {
        return new Color((int)(c.getRed() * 0.2126), (int)(c.getGreen() * 0.7152), (int)(c.getBlue() * 0.0722), c.getAlpha());
    }

}
