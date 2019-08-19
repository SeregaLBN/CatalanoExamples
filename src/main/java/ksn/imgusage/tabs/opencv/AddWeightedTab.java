package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import ksn.imgusage.model.SliderDoubleModel;
import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.tabs.ITab;
import ksn.imgusage.type.dto.opencv.AddWeightedTabParams;

/** <a href='https://docs.opencv.org/3.4/d2/de8/group__core__array.html#gafafb2513349db3bcff51f54ee5592a19'>Calculates the weighted sum of two arrays</a> */
public class AddWeightedTab extends OpencvFilterTab<AddWeightedTabParams> {

    public static final String TAB_TITLE = "AddWeighted";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Calculates the weighted sum of two arrays";

    private static final double MIN_ALPHA = -200;
    private static final double MAX_ALPHA =  200;
    public  static final double MIN_BETA  = -200;
    private static final double MAX_BETA  =  200;
    public  static final double MIN_GAMMA = -500;
    private static final double MAX_GAMMA =  500;
    public  static final int    MIN_DEPTH =   -1;
    private static final int    MAX_DEPTH =   99;

    private ITab<?> src2;
    private AddWeightedTabParams params;

    public void setSource2(ITab<?> src2) {
        this.src2 = src2;
    }

    @Override
    public Component makeTab(AddWeightedTabParams params) {
        if (params == null)
            params = new AddWeightedTabParams();
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
        if (src2 == null)
            throw new IllegalArgumentException("Don`t defined secondary source!");

        // TODO Note
        // Saturation is not applied when the output array has the depth CV_32S. You may even get result of an incorrect sign in the case of overflow

        Mat dst = new Mat();
        Core.addWeighted(imageMat, params.alpha, getSourceMat(src2), params.beta, params.gamma, dst, params.dtype);
        imageMat = dst;
    }


    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(""));

        SliderDoubleModel modelAlpha = new SliderDoubleModel(params.alpha, 0, MIN_ALPHA, MAX_ALPHA);
        SliderDoubleModel modelBeta  = new SliderDoubleModel(params.beta , 0, MIN_BETA , MAX_BETA);
        SliderDoubleModel modelGamma = new SliderDoubleModel(params.gamma, 0, MIN_GAMMA, MAX_GAMMA);
        SliderIntModel    modelDType = new    SliderIntModel(params.dtype, 0, MIN_DEPTH, MAX_DEPTH);

        Box box4Sliders = Box.createHorizontalBox();
        box4Sliders.setToolTipText("The function addWeighted calculates the weighted sum of two arrays as follows:" +
                "\n 𝚍𝚜𝚝(I)=𝚜𝚊𝚝𝚞𝚛𝚊𝚝𝚎(𝚜𝚛𝚌𝟷(I)∗𝚊𝚕𝚙𝚑𝚊+𝚜𝚛𝚌𝟸(I)∗𝚋𝚎𝚝𝚊+𝚐𝚊𝚖𝚖𝚊)" +
                "\n where I is a multi-dimensional index of array elements. In case of multi-channel arrays, each channel is processed independently. The function can be replaced with a matrix expression:" +
                "\n dst = src1*alpha + src2*beta + gamma");
        box4Sliders.add(Box.createHorizontalGlue());
        box4Sliders.add(makeSliderVert(modelAlpha, "Alpha", "Weight of the first array elements"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelBeta , "Beta" , "Weight of the second array elements"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelGamma, "Gamma", "Scalar added to each sum"));
        box4Sliders.add(Box.createHorizontalStrut(2));
        box4Sliders.add(makeSliderVert(modelDType, "dType", "Optional depth of the output array; when both input arrays have the same depth, dtype can be set to -1, which will be equivalent to src1.depth()"));
        box4Sliders.add(Box.createHorizontalGlue());


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box4Sliders, BorderLayout.CENTER);

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
        modelGamma.getWrapped().addChangeListener(ev -> {
            logger.trace("modelGamma: value={}", modelGamma.getFormatedText());
            params.gamma = modelGamma.getValue();
            resetImage();
        });
        modelDType.getWrapped().addChangeListener(ev -> {
            logger.trace("modelDType: value={}", modelDType.getFormatedText());
            params.dtype = modelDType.getValue();
            resetImage();
        });

        return box4Options;
    }

    @Override
    public AddWeightedTabParams getParams() {
        return params;
    }

}
