package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.opencv.core.Mat;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.ContrastAndBrightnessTabParams;

/** <a href='https://docs.opencv.org/3.4/d3/dc1/tutorial_basic_linear_transform.html'>Changing the contrast and brightness of an image</a> */
public class ContrastAndBrightnessTab extends OpencvFilterTab<ContrastAndBrightnessTabParams> {

    public static final String TAB_TITLE = "Contrast/Brightness";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Changing the contrast and brightness of an image";

    private static final double MIN_ALPHA =    0;
    private static final double MAX_ALPHA =   10;
    public  static final int    MIN_BETA  = -200;
    private static final int    MAX_BETA  =  200;

    private ContrastAndBrightnessTabParams params;

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


    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelAlpha = new SliderDoubleModel(params.alpha, 0, MIN_ALPHA, MAX_ALPHA);
        SliderIntModel    modelBeta  = new    SliderIntModel(params.beta , 0, MIN_BETA , MAX_BETA);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.setToolTipText("Two commonly used point processes are multiplication and addition with a constant: g(x)=αf(x)+β");
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelAlpha, "Alpha", "The parameters α>0 and β are often called the gain and bias parameters; sometimes these parameters are said to control contrast and brightness respectively"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelBeta , "Beta", "You can think of f(x) as the source image pixels and g(x) as the output image pixels. Then, more conveniently we can write the expression as:" +
                "\n g(i,j)=α⋅f(i,j)+β \n" +
                "\n where i and j indicates that the pixel is located in the i-th row and j-th column"));
        box4Sliders.add(Box.createHorizontalGlue());


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders   , BorderLayout.CENTER);

        box4Options.add(panelOptions);

        modelAlpha.getWrapped().addChangeListener(ev -> {
            logger.trace("modelAlpha: value={}", modelAlpha.getFormatedText());
            params.alpha = modelAlpha.getValue();
            resetImage();
        });
        modelBeta.getWrapped().addChangeListener(ev -> {
            logger.trace("modelBeta: value={}", modelBeta.getFormatedText());
            params.beta = modelBeta.getValue();
            resetImage();
        });

        return box4Options;
    }

    @Override
    public ContrastAndBrightnessTabParams getParams() {
        return params;
    }

}
