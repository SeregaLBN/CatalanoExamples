package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.type.dto.opencv.WatershedTabParams;
import ksn.imgusage.type.opencv.CvDepthType;
import ksn.imgusage.type.opencv.CvLineType;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4.2/d7/d1b/group__imgproc__misc.html#ga3267243e4d3f95165d55a618c65ac6e1'>Performs a marker-based image segmentation using the watershed algorithm</a> */
public class WatershedTab extends OpencvFilterTab<WatershedTabParams> {

    public static final String TAB_TITLE = "Watershed";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Performs a marker-based image segmentation using the watershed algorithm";

    private WatershedTabParams params;

    @Override
    public Component makeTab(WatershedTabParams params) {
        this.params = params;
        return makeTab();
    }

    @Override
    public String getTitle() { return TAB_TITLE; }
    @Override
    public String getName() { return TAB_NAME; }
    @Override
    public String getDescription() { return TAB_DESCRIPTION; }

    @Override
    protected void applyOpencvFilter() {
        /*
         1. Mat img - прочитали файл картинки
         2. Mat markerMask - создали её серую копию
         3. Mat imgGray - создали из серой копии цветную
         4. вызвали findContours для markerMask
         5. Mat markers - создали чёрный (CV_32S)
         6. drawContours - на markers рисую все контуры (случ цветом?)
         7. colorTab - содлал лист случ светов, размерность - кол-во контуров
         8. watershed(img, markers);
         9. Mat wshed(markers.size(), CV_8UC3);
        10. прохожу по всем маркерам (из markers)
            и для каждого валидного - беру цвет из colorTab
        11. wshed = wshed*0.5 + imgGray*0.5;
         */


        // cast to gray image
        Mat markerMask = OpenCvHelper.toGray(imageMat);
        Mat imgGray = new Mat();
        Imgproc.cvtColor(markerMask, imgGray, Imgproc.COLOR_GRAY2BGR);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            markerMask, // src
            contours, // out
            hierarchy, // dst
            Imgproc.RETR_CCOMP,
            Imgproc.CHAIN_APPROX_SIMPLE);

        if (!true) {
            Mat markers = new Mat(imageMat.size(), CvType.CV_32S, Scalar.all(255));
            int compCount = 0;
            int intMax = +2147483647; // C lang INT_MAX
            Point offset = new Point();
            for (int idx = 0; idx >= 0; compCount++) {
                Imgproc.drawContours(markers, contours, idx, Scalar.all(compCount + 1), -1, 8, hierarchy, intMax, offset);
                // idx = hierarchy[idx][0],
                int[] data = { 0, 0, 0, 0 };
                int readed = hierarchy.get(idx, 0, data);
                if (readed < 1)
                    idx = -1;
                else
                    idx = data[0];
            }
            logger.trace("compCount={}", compCount);
            return;

        }

        int compCount = contours.size();
        Random rnd = ThreadLocalRandom.current();
        byte[][] colorTab = new byte[compCount][];
        for (int i = 0; i < compCount; ++i) {
            byte[] rgb = new byte[3];
            rnd.nextBytes(rgb);
            colorTab[i] = rgb;
        }

        Mat markers = new Mat(imageMat.size(), CvType.CV_32S, Scalar.all(0));
        Scalar green = new Scalar(0, 255, 0);
        Imgproc.drawContours(markers,            // Mat image
                             contours,           // List<MatOfPoint> contours
                             -1,                 // int contourIdx
                             green,              // Scalar color
                             true               // int thickness
                                 ? CvLineType.FILLED.getVal()
                                 : 1,
                             CvLineType.LINE_AA.getVal(),     // int lineType
                             hierarchy,                       // Mat hierarchy
                             3,                               // int maxLevel
                             new Point()                      // Point offset
                         );

        Imgproc.watershed(
            OpenCvHelper.to3Channel(imageMat), // src  // Input 8-bit 3-channel image.
            markers);

        Mat wshed = new Mat(markers.size(), CvType.CV_8UC3);
        logger.trace("wshed depth={}", CvDepthType.fromValue(CvType.depth(wshed.type())));

        // paint the watershed image
        byte[] clrWhite = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF };
        byte[] clrBlack = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00 };
        for (int i=0; i < markers.rows(); ++i) {
            for (int j=0; j < markers.cols(); ++j) {
                int[] data = { 0 };
                int offset = markers.get(i, j, data);
                if (offset < 1)
                    continue;
                int index = data[0];
                if (index == -1)
                    wshed.put(i, j, clrWhite);
                else if (index <= 0 || index > compCount)
                    wshed.put(i, j, clrBlack);
                else
                    wshed.put(i, j, colorTab[index - 1]);
            }
        }

        // wshed = wshed*0.5 + imgGray*0.5;
        Mat dst = new Mat();
        Core.addWeighted(wshed, 0.5, imgGray, 0.5, 0, dst);
        imageMat = dst;
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        // none..

        return box4Options;
    }

    @Override
    public WatershedTabParams getParams() {
        return params;
    }

}
