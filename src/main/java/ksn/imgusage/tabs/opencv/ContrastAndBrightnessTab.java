package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.math3.util.Pair;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.ContrastAndBrightnessTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4/d3/dc1/tutorial_basic_linear_transform.html'>Changing the contrast and brightness of an image</a> */
public class ContrastAndBrightnessTab extends OpencvFilterTab<ContrastAndBrightnessTabParams> {

    public static final String TAB_TITLE = "Contrast/Brightness";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Changing the contrast and brightness of an image";

    private static final double MIN_ALPHA =    0;
    private static final double MAX_ALPHA =   10;
    public  static final double MIN_BETA  = -200;
    private static final double MAX_BETA  =  250;

    private ContrastAndBrightnessTabParams params;
    /** histogram clipping in percent 1..99 */
    private int clipHistPercent = 25;

    @Override
    public Component makeTab(ContrastAndBrightnessTabParams params) {
        if (params == null)
            params = new ContrastAndBrightnessTabParams();
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
        Mat dst = new Mat();
        Core.convertScaleAbs(imageMat, dst, params.alpha, params.beta);
        imageMat = dst;
    }

    /** /
    @Override
    protected void applyOpencvFilter() {
        Mat newImage = Mat.zeros(imageMat.size(), imageMat.type());

        byte[] imageData = new byte[(int) (imageMat.total() * imageMat.channels())];
        imageMat.get(0, 0, imageData);
        byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];
        for (int y = 0; y < imageMat.rows(); y++) {
            for (int x = 0; x < imageMat.cols(); x++) {
                for (int c = 0; c < imageMat.channels(); c++) {
                    double pixelValue = imageData[(y * imageMat.cols() + x) * imageMat.channels() + c];
                    pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
                    newImageData[(y * imageMat.cols() + x) * imageMat.channels() + c] =
                            saturate(params.alpha * pixelValue + params.beta);
                }
            }
        }
        newImage.put(0, 0, newImageData);

        imageMat = newImage;
    }

    private static byte saturate(double val) {
        int iVal = (int)Math.round(val);
        if (iVal > 255)
            return (byte)255;
        if (iVal < 0)
            return 0;
        return (byte)iVal;
    }
    /**/

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelAlpha = new SliderDoubleModel(params.alpha, 0, MIN_ALPHA, MAX_ALPHA);
        SliderDoubleModel modelBeta  = new SliderDoubleModel(params.beta , 0, MIN_BETA , MAX_BETA);
        SliderIntModel    modelClipHist = new SliderIntModel(clipHistPercent, 0, 1, 99);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.setToolTipText("Two commonly used point processes are multiplication and addition with a constant: g(x)=αf(x)+β");
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelAlpha, "Alpha", "The parameters α>0 and β are often called the gain and bias parameters; sometimes these parameters are said to control contrast and brightness respectively"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelBeta , "Beta", "You can think of f(x) as the source image pixels and g(x) as the output image pixels. Then, more conveniently we can write the expression as:" +
                "\n g(i,j)=α⋅f(i,j)+β \n" +
                "\n where i and j indicates that the pixel is located in the i-th row and j-th column"));
        box4Sliders.add(Box.createHorizontalGlue());


        Container cntrlHistClip = makeEditBox("clipHistPercent", modelClipHist, "Histogram clipping", null, null);
        JButton btnAuto = new JButton("Apply..");
        btnAuto.setToolTipText("Automatic brightness and contrast optimization");
        btnAuto.addActionListener(ev -> {
            Mat sourceMat = getSourceMat();
            if (sourceMat == null)
                return;

            Pair<Double, Double> val = automaticBrightnessAndContrast(sourceMat, clipHistPercent);
            modelAlpha.setValue(val.getFirst());
            modelBeta.setValue(val.getSecond());
        });

        Box boxFindAutoParams = Box.createHorizontalBox();
        boxFindAutoParams.setBorder(BorderFactory.createTitledBorder("Automatic brightness and contrast"));
        boxFindAutoParams.setToolTipText("Find brightness and contrast");
        boxFindAutoParams.add(Box.createHorizontalGlue());
        boxFindAutoParams.add(cntrlHistClip);
        boxFindAutoParams.add(Box.createHorizontalGlue());
        boxFindAutoParams.add(btnAuto);
        boxFindAutoParams.add(Box.createHorizontalGlue());


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(boxFindAutoParams, BorderLayout.NORTH);
        panelOptions.add(box4Sliders, BorderLayout.CENTER);

        box4Options.add(panelOptions);

        addChangeListener("params.alpha"    , modelAlpha   , v -> params.alpha    = v);
        addChangeListener("params.beta"     , modelBeta    , v -> params.beta     = v);
        addChangeListener("clipHistPercent" , modelClipHist, v -> clipHistPercent = v);

        return box4Options;
    }

    @Override
    public ContrastAndBrightnessTabParams getParams() {
        return params;
    }

    private static Pair<Double, Double> automaticBrightnessAndContrast(Mat image, int clipHistPercent /* = 25 */) {
        Mat gray = OpenCvHelper.toGray(image);

        // Calculate grayscale histogram
        Mat hist = new Mat();
        Imgproc.calcHist(
             Arrays.asList(gray),
             new MatOfInt(0),
             new Mat(),
             hist,
             new MatOfInt(256),
             new MatOfFloat(0, 256));
        Size histSize0 = hist.size();
        int histSize = (int)histSize0.height;

        // Calculate cumulative distribution from the histogram
        float[] accumulator = new float[histSize];
        float[] val = {0};
        for (int i = 0; i < histSize; ++i) {
            int res = hist.get(i, 0, val);
            assert res == 4; // 4 bytes read
            accumulator[i] = (i == 0)
                    ? val[0]
                    : val[0] + accumulator[i - 1];
        }

        // Locate points to clip
        float maximum = accumulator[accumulator.length - 1];
        clipHistPercent *= maximum / 100.0;
        clipHistPercent /= 2;

        // Locate left cut
        int minimumGray = 0;
        while (accumulator[minimumGray] < clipHistPercent) {
            ++minimumGray;
            if (minimumGray >= histSize)
                break;
        }

        // Locate right cut
        int maximumGray = histSize - 1;
        while (accumulator[maximumGray] >= (maximum - clipHistPercent)) {
            --maximumGray;
            if (maximumGray < 0)
                break;
        }

        // Calculate alpha and beta values
        double alpha = 255.0 / (maximumGray - minimumGray);
        double beta = -minimumGray * alpha;

        return new Pair<>(alpha, beta);
    }

}
