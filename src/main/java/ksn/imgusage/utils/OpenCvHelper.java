package ksn.imgusage.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public final class OpenCvHelper {
    private OpenCvHelper() {}

    public static Mat fromImage(BufferedImage image) {
        Mat mat = fromImageFast(image);
        if (mat == null)
            mat = fromImageUniversal(image);
        return mat;
    }

    public static BufferedImage toImage(Mat mat) {
        BufferedImage img = toImageFast(mat);
        if (img == null)
            img = toImageUniversal(mat);
        return img;
    }

    private static BufferedImage toImageFast(Mat mat) {
        switch (mat.channels()) {
        case 1: {
                System.err.println("OpenCvHelper::toImageFast: TODO: Don`t verified: chanels 1");
                // TODO
                return null;
            }
        case 3: {
                System.err.println("OpenCvHelper::toImageFast: TODO: Don`t verified: chanels 3");
                int bufferSize = mat.channels() * mat.cols() * mat.rows();
                byte[] b = new byte[bufferSize];
                BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_INT_RGB);

                int[] targetPixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
                System.arraycopy(b, 0, targetPixels, 0, b.length);
                return image;
            }
        case 4: {
                System.err.println("OpenCvHelper::toImageFast: TODO: Don`t verified: chanels 4");
                // TODO
                return null;
            }
        default:
            return null;
        }
    }

    private static Mat fromImageFast(BufferedImage image) {
        int colorType = image.getType();
        switch (colorType) {
        case BufferedImage.TYPE_INT_RGB: {
                Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
                DataBufferInt intBuff = (DataBufferInt)image.getRaster().getDataBuffer();
                int[] bufferFrom = intBuff.getData();
                byte[] bufferTo = new byte[image.getWidth() * image.getHeight() * (int)mat.elemSize()];
                assert (bufferFrom.length * 3) == bufferTo.length;
                int tmp;
                for (int i = 0, j = 0; i < bufferFrom.length; i++) {
                    tmp = bufferFrom[i];
                    bufferTo[j++] = (byte)((tmp >>  0) & 0xFF);
                    bufferTo[j++] = (byte)((tmp >>  8) & 0xFF);
                    bufferTo[j++] = (byte)((tmp >> 16) & 0xFF);
                }
                mat.put(0, 0, bufferTo);
                return mat;
            }
        case BufferedImage.TYPE_INT_ARGB: {
                Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                DataBufferInt intBuff = (DataBufferInt)image.getRaster().getDataBuffer();
                int[] bufferFrom = intBuff.getData();
                byte[] bufferTo = new byte[image.getWidth() * image.getHeight() * (int)mat.elemSize()];
                assert (bufferFrom.length * 4) == bufferTo.length;
                int tmp;
                for (int i = 0, j = 0; i < bufferFrom.length; i++) {
                    tmp = bufferFrom[i];
                    bufferTo[j++] = (byte)((tmp >>  0) & 0xFF);
                    bufferTo[j++] = (byte)((tmp >>  8) & 0xFF);
                    bufferTo[j++] = (byte)((tmp >> 16) & 0xFF);
                    bufferTo[j++] = (byte)((tmp >> 24) & 0xFF);
                }
                mat.put(0, 0, bufferTo);
                return mat;
            }
        case BufferedImage.TYPE_BYTE_GRAY: {
                Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
                DataBufferByte byteBuff = (DataBufferByte)image.getRaster().getDataBuffer();
                mat.put(0, 0, byteBuff.getData());
                return mat;
            }
        case BufferedImage.TYPE_3BYTE_BGR: {
                System.err.println("OpenCvHelper::fromImage: TODO: Don`t verified: BufferedImage.TYPE_3BYTE_BGR");
                Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, ((DataBufferByte)image.getRaster().getDataBuffer()).getData());
                return mat;
            }
        default:
            return null;
        }
    }

    private static Mat fromImageUniversal(BufferedImage image) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static BufferedImage toImageUniversal(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        try (InputStream in = new ByteArrayInputStream(mob.toArray())) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Mat toGray(Mat from) {
        switch (from.channels()) {
        case 1: return from;
        case 3: {
                Mat to = new Mat(from.height(), from.width(), CvType.CV_8UC1);
                Imgproc.cvtColor(from, to, Imgproc.COLOR_RGB2GRAY);
                return to;
            }
        case 4: {
                Mat to = new Mat(from.height(), from.width(), CvType.CV_8UC1);
                Imgproc.cvtColor(from, to, Imgproc.COLOR_RGBA2GRAY);
                return to;
            }
        default:
            throw new RuntimeException("Unsupported");
        }
    }

}
