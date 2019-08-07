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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpenCvHelper {
    private OpenCvHelper() {}

    private static final Logger logger = LoggerFactory.getLogger(OpenCvHelper.class);



    /** @see <a href='https://docs.opencv.org/3.4.2/d2/de8/group__core__array.html#ga209f2f4869e304c82d07739337eae7c5'>Various border types, image boundaries are denoted with `|`</a> */
    public enum BorderTypes {
        /** `iiiiii|abcdefgh|iiiiiii`  with some specified `i` */
        BORDER_CONSTANT   (0),
        /** `aaaaaa|abcdefgh|hhhhhhh` */
        BORDER_REPLICATE  (1),
        /** `fedcba|abcdefgh|hgfedcb` */
        BORDER_REFLECT    (2),
        /** `cdefgh|abcdefgh|abcdefg` */
        BORDER_WRAP       (3),
        /** `gfedcb|abcdefgh|gfedcba` */
        BORDER_REFLECT_101(4),
        /** `uvwxyz|abcdefgh|ijklmno` */
        BORDER_TRANSPARENT(5),

      ///** same as BORDER_REFLECT_101 */
      //BORDER_REFLECT101 (BORDER_REFLECT_101), //!<
        /** same as {@link #BORDER_REFLECT_101} */
        BORDER_DEFAULT    (BORDER_REFLECT_101.val),
        /** do not look outside of ROI (Region Of Interest) */
        BORDER_ISOLATED   (16);

        private final int val;
        private BorderTypes(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

    }

    /** @see <a href="https://docs.opencv.org/3.4.2/d4/d86/group__imgproc__filter.html#ga7be549266bad7b2e6a04db49827f9f32">type of morphological operation</a> */
    public enum MorphTypes {
        /** Erodes an image by using a specific structuring element. */
        MORPH_ERODE    (0),
        /** Dilates an image by using a specific structuring element. */
        MORPH_DILATE   (1),
        /** an opening operation
            <p>𝚍𝚜𝚝=open(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=dilate(erode(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)) */
        MORPH_OPEN     (2),
        /** a closing operation
            <p>𝚍𝚜𝚝=close(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=erode(dilate(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)) */
        MORPH_CLOSE    (3),
        /** a morphological gradient
            <p>𝚍𝚜𝚝=morph_grad(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=dilate(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)−erode(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝) */
        MORPH_GRADIENT (4),
        /** "top hat"
            <p>𝚍𝚜𝚝=tophat(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=𝚜𝚛𝚌−open(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝) */
        MORPH_TOPHAT   (5),
        /** "black hat"
            <p>𝚍𝚜𝚝=blackhat(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)=close(𝚜𝚛𝚌,𝚎𝚕𝚎𝚖𝚎𝚗𝚝)−𝚜𝚛𝚌 */
        MORPH_BLACKHAT (6),
        /** "hit or miss" .- Only supported for CV_8UC1 binary images. A tutorial can be found in the documentation */
        MORPH_HITMISS  (7);

        private final int val;
        private MorphTypes(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

    }



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
        int channels = mat.channels();
        switch (channels) {
        case 1: return toImageFast1Channel(mat);
        case 3: return toImageFast3Channel(mat);
        case 4: return toImageFast4Channel(mat);
        default:
            logger.warn("OpenCvHelper::toImageFast: Try add for Mat::channels == {}", channels);
            return null;
        }
    }

    private static BufferedImage toImageFast1Channel(Mat mat) {
        int depth = CvType.depth(mat.type());
        switch (depth) {
        case CvType.CV_8U: {
                byte[] matBuff = new byte[mat.cols() * mat.rows() * (int)mat.elemSize()];
                mat.get(0, 0, matBuff);

                BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
                byte[] imgBuff = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
                assert imgBuff.length == matBuff.length;
                System.arraycopy(matBuff, 0, imgBuff, 0, imgBuff.length);
                return image;
            }
        default:
            logger.warn("OpenCvHelper::toImageFast1Channel: Try add for depth == {}", depth);
            return null;
        }
    }

    private static BufferedImage toImageFast3Channel(Mat mat) {
        int depth = CvType.depth(mat.type());
        switch (depth) {
        case CvType.CV_8U: {
                byte[] matBuff = new byte[mat.cols() * mat.rows() * (int)mat.elemSize()];
                mat.get(0, 0, matBuff);

                BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
                byte[] imgBuff = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
                assert imgBuff.length == matBuff.length;
                System.arraycopy(matBuff, 0, imgBuff, 0, imgBuff.length);
                return image;
            }
        default:
            logger.warn("OpenCvHelper::toImageFast3Channel: Try add for depth == {}", depth);
            return null;
        }
    }

    private static BufferedImage toImageFast4Channel(Mat mat) {
        int depth = CvType.depth(mat.type());
//        switch (depth) {
//        case CvType.CV_8U: {
//                System.err.println("OpenCvHelper::toImageFast4Channel: TODO: Don`t verified: depth CvType.CV_8U");
//                byte[] matBuff = new byte[mat.cols() * mat.rows() * (int)mat.elemSize()];
//                mat.get(0, 0, matBuff);
//
//                BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_4BYTE_ABGR);
//                byte[] imgBuff = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
//                assert imgBuff.length == matBuff.length;
//                System.arraycopy(matBuff, 0, imgBuff, 0, imgBuff.length);
//
////                //  R <-> B
////                byte tmp;
////                for (int i = 0; i < imgBuff.length; i += 4) {
////                    tmp = imgBuff[i + 3];
////                    imgBuff[i + 0] = imgBuff[i + 1];
////                    imgBuff[i + 1] = imgBuff[i + 2];
////                    imgBuff[i + 2] = tmp;
////                    imgBuff[i + 3] = tmp;
////                }
//
//                return image;
//            }
//        default:
            logger.warn("OpenCvHelper::toImageFast4Channel: Try add for depth == {}", depth);
            return null;
//        }
    }

    private static Mat fromImageFast(BufferedImage image) {
        int imageType = image.getType();
        switch (imageType) {
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
                logger.warn("OpenCvHelper::fromImage: TODO: Don`t verified: BufferedImage.TYPE_3BYTE_BGR");
                Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, ((DataBufferByte)image.getRaster().getDataBuffer()).getData());
                return mat;
            }
        default:
            logger.warn("OpenCvHelper::fromImage: Try add for imageType={}", imageType);
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
