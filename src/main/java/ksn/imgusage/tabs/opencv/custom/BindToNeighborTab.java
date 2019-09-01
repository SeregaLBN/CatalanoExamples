package ksn.imgusage.tabs.opencv.custom;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ksn.imgusage.model.SliderIntModel;
import ksn.imgusage.type.dto.opencv.custom.BindToNeighborTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** Connect the nearest regions */
public class BindToNeighborTab extends CustomTab<BindToNeighborTabParams> {

    public static final String TAB_TITLE = "BindToNeighborTab";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Connect the nearest regions";

    private static final int MIN_MIN_LIMIT_CONTOUR_SIZE =    0;
    private static final int MAX_MIN_LIMIT_CONTOUR_SIZE =  995;
    private static final int MIN_MAX_LIMIT_CONTOUR_SIZE =    5;
    private static final int MAX_MAX_LIMIT_CONTOUR_SIZE = 1000;

    private static final Scalar green = new Scalar(0, 255, 0);
    private static final Scalar red = new Scalar(0, 0, 255);
    private static final Scalar orange = new Scalar(Color.ORANGE.getBlue(), Color.ORANGE.getGreen(), Color.ORANGE.getRed());
    private static final Scalar white = new Scalar(Color.LIGHT_GRAY.getBlue(), Color.LIGHT_GRAY.getGreen(), Color.LIGHT_GRAY.getRed());

    private BindToNeighborTabParams params;
    private IntConsumer setMaxBindIndex;

    @Override
    public Component makeTab(BindToNeighborTabParams params) {
        if (params == null)
            params = new BindToNeighborTabParams();
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
        // cast to gray image
        imageMat = OpenCvHelper.toGray(imageMat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            imageMat,  // src
            contours,  // out1
            hierarchy, // out2
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            logger.warn("No any contours found!");
            return;
        }


        List<Rect> limited = contours.stream()
            .map(Imgproc::boundingRect)
            .filter(rc -> (rc.width >= params.minLimitContours.width) && (rc.height >= params.minLimitContours.height))
            .filter(rc -> (rc.width <= params.maxLimitContours.width) && (rc.height <= params.maxLimitContours.height))
            .collect(Collectors.toList());
        SwingUtilities.invokeLater(() -> setMaxBindIndex.accept(limited.size() - 1));

        List<List<BindItem>> bindings = bind(limited);
        bindings = filterNeighbor(bindings);

        { // restore color image
            Mat mat = new Mat();
            Imgproc.cvtColor(imageMat, mat, Imgproc.COLOR_GRAY2RGB);
            imageMat = mat;
        }

        if (params.showBindIndex < 0) {
            // show all
            for (int i = 0; i< limited.size(); ++i)
                drawLine(i, limited, bindings, false, false, true);
        } else {
            // show one
            drawLine(params.showBindIndex, limited, bindings, true, true, false);
        }
    }

    private void drawLine(
        int index,
        List<Rect> regions,
        List<List<BindItem>> bindings,
        boolean showRectRegion,
        boolean showMinBindSize,
        boolean skipUp
    ) {
        Rect from = regions.get(index);
        List<BindItem> toList = bindings.get(index);

        for (BindItem item : toList) {
            if (skipUp && (index < item.to))
                continue;
            Rect to = regions.get(item.to);

            Point centerFrom = new Point(from.x + from.width / 2.0, from.y + from.height / 2.0);
            Point centerTo   = new Point(to  .x + to  .width / 2.2, to  .y + to  .height / 2.0);
            Imgproc.line(imageMat, centerFrom, centerTo, white, params.bindSize * 2);
        }

        // draw rects/lines ower/after bold lines
        if (showRectRegion || showMinBindSize) {
            if (showRectRegion)
                Imgproc.rectangle(imageMat,
                                  new Point(from.x, from.y),
                                  new Point(from.x + from.width, from.y + from.height),
                                  green, 1);

            for (BindItem item : toList) {
                Rect to = regions.get(item.to);

                if (showRectRegion)
                    Imgproc.rectangle(imageMat,
                                      new Point(to.x, to.y),
                                      new Point(to.x + to.width, to.y + to.height),
                                      red, 1);
                if (showMinBindSize)
                    Imgproc.line(imageMat, item.minFrom, item.minTo, orange, 2);
            }
        }
    }

    static class BindItem {
        int to;
        EAlignX alignX;
        EAlignY alignY;
        Point minFrom;
        Point minTo;
    }

    private List<List<BindItem>> filterNeighbor(List<List<BindItem>> bindings) {
        List<List<BindItem>> toDeleteAll = new ArrayList<>();

        // delete by diagonal if exist perpendicular
        for (int i = 0; i < bindings.size(); ++ i) {
            List<BindItem> toDelete = new ArrayList<>();
            toDeleteAll.add(toDelete);

            List<BindItem> neighbors = bindings.get(i);

            BiConsumer<Predicate<BindItem>, Predicate<BindItem>> check = (alignAxis, alignCenter) -> {
                List<BindItem> byAxis = neighbors.stream()
                    .filter(alignAxis::test)
                    .collect(Collectors.toList());
                if (byAxis.size() > 1) {
                    BindItem center = byAxis.stream()
                            .filter(alignCenter::test)
                            .findAny()
                            .orElseGet(() -> null);
                    if (center != null)
                        byAxis.stream()
                            .filter(item -> item != center)
                            .forEach(toDelete::add);
                }
            };
            check.accept(item -> item.alignY == EAlignY.top   , item -> item.alignX == EAlignX.center);
            check.accept(item -> item.alignY == EAlignY.bottom, item -> item.alignX == EAlignX.center);
            check.accept(item -> item.alignX == EAlignX.left  , item -> item.alignY == EAlignY.center);
            check.accept(item -> item.alignX == EAlignX.right , item -> item.alignY == EAlignY.center);
        }

        List<List<BindItem>> res = new ArrayList<>(bindings.size());
        for (int i = 0; i < bindings.size(); ++ i) {
            List<BindItem> toDelete = toDeleteAll.get(i);
            List<BindItem> neighbors = bindings.get(i);
            List<BindItem> toAdd = neighbors.stream()
                       .filter(item -> !toDelete.contains(item))
                       .collect(Collectors.toList());
            res.add(toAdd);
        }
        return res;
    }

    private List<List<BindItem>> bind(List<Rect> singles) {
        List<List<BindItem>> res = new ArrayList<>(singles.size());
        for (int i = 0; i < singles.size(); ++i) {
            List<BindItem> to = new ArrayList<>();
            res.add(to);

            Rect rcFrom = singles.get(i);
            for (int j = 0; j < singles.size(); ++j) {
                if (i == j)
                    continue;
                Rect rcTo = singles.get(j);

                EAlignX alignX = getAlignX(rcFrom, rcTo);
                EAlignY alignY = getAlignY(rcFrom, rcTo);
                Point minFrom;
                Point minTo;
                switch (alignX) {
                case left:
                    switch (alignY) {
                    case top:
                        minFrom = new Point(rcFrom.x, rcFrom.y);
                        minTo   = new Point(rcTo.x + rcTo.width, rcTo.y + rcTo.height);
                        break;
                    case center:
                        minFrom = new Point(rcFrom.x           , Math.min(rcFrom.y, rcTo.y + rcTo.height));
                        minTo   = new Point(rcTo.x + rcTo.width, minFrom.y);
                        break;
                    case bottom:
                        minFrom = new Point(rcFrom.x, rcFrom.y + rcFrom.height);
                        minTo   = new Point(rcTo.x + rcTo.width, rcTo.y);
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                    break;

                case center:
                    switch (alignY) {
                    case top:
                        minFrom = new Point(Math.min(rcFrom.x, rcTo.x), rcFrom.y);
                        minTo   = new Point(minFrom.x, rcTo.y + rcTo.height);
                        break;
                    case center:
                        logger.warn("...........");
                        continue;
                    case bottom:
                        minFrom = new Point(Math.min(rcFrom.x, rcTo.x), rcFrom.y + rcFrom.height);
                        minTo   = new Point(minFrom.x, rcTo.y);
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                    break;

                case right:
                    switch (alignY) {
                    case top:
                        minFrom = new Point(rcFrom.x + rcFrom.width, rcFrom.y);
                        minTo   = new Point(rcTo.x, rcTo.y + rcTo.height);
                        break;
                    case center:
                        minFrom = new Point(rcFrom.x + rcFrom.width, Math.min(rcFrom.y, rcTo.y + rcTo.height));
                        minTo   = new Point(rcTo.x, minFrom.y);
                        break;
                    case bottom:
                        minFrom = new Point(rcFrom.x + rcFrom.width, rcFrom.y + rcTo.height);
                        minTo   = new Point(rcTo.x, rcTo.y);
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                    break;

                default:
                    throw new IllegalArgumentException();
                }

                double diffX = Math.abs(minFrom.x - minTo.x);
                double diffY = Math.abs(minFrom.y - minTo.y);
                double diff = Math.sqrt(diffX * diffX + diffY * diffY);

                if (diff <= params.bindSize) {
                    BindItem item = new BindItem();
                    item.to = j;
                    item.alignX = alignX;
                    item.alignY = alignY;
                    item.minFrom = minFrom;
                    item.minTo = minTo;
                    to.add(item);
                }
            }
        }
        return res;
    }

    private EAlignX getAlignX(Rect from, Rect to) {
        if ((to.x + to.width) < from.x)
            return EAlignX.left;

        if ((from.x + from.width) < to.x)
            return EAlignX.right;

        int centerToX   = to.x   + to.width  / 2;
        int centerFromX = from.x + from.width / 2;

        if ((centerFromX - centerToX) >= params.bindSize)
            return EAlignX.left;
        if ((centerToX - centerFromX) >= params.bindSize)
            return EAlignX.right;

        return EAlignX.center;
    }

    private EAlignY getAlignY(Rect from, Rect to) {
        if ((to.y + to.height) < from.y)
            return EAlignY.top;

        if ((from.y + from.height) < to.y)
            return EAlignY.bottom;

        int centerToY   = to.y   + to.height / 2;
        int centerFromY = from.y + from.height / 2;

        if ((centerFromY - centerToY) >= params.bindSize)
            return EAlignY.top;
        if ((centerToY - centerFromY) >= params.bindSize)
            return EAlignY.bottom;

        return EAlignY.center;
    }

    enum EAlignX {
        left, center, right
    }
    enum EAlignY {
        top, center, bottom
    }

    @Override
    protected Component makeOptions() {
        Box box4Options = Box.createVerticalBox();
        box4Options.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));

        SliderIntModel modelMinLimitContoursW = new SliderIntModel(params.minLimitContours.width , 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMinLimitContoursH = new SliderIntModel(params.minLimitContours.height, 0, MIN_MIN_LIMIT_CONTOUR_SIZE, MAX_MIN_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMaxLimitContoursW = new SliderIntModel(params.maxLimitContours.width , 0, MIN_MAX_LIMIT_CONTOUR_SIZE, MAX_MAX_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelMaxLimitContoursH = new SliderIntModel(params.maxLimitContours.height, 0, MIN_MAX_LIMIT_CONTOUR_SIZE, MAX_MAX_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelBindSize          = new SliderIntModel(params.bindSize, 0, 1, MAX_MAX_LIMIT_CONTOUR_SIZE);
        SliderIntModel modelShowBindIndex     = new SliderIntModel(params.showBindIndex, 0, -1, 999999);
        setMaxBindIndex = modelShowBindIndex::setMaximum;

        Component cntrlMinLimit = makeSize(modelMinLimitContoursW,  // modelSizeW
                                           modelMinLimitContoursH,    // modelSizeH
                                           "MinLimitContour",         // borderTitle
                                           null,                      // tip
                                           "MinLimitContour.Width",   // tipWidth
                                           "MinLimitContour.Height"); // tipHeight
        Component cntrlMaxLimit = makeSize(modelMaxLimitContoursW,  // modelSizeW
                                           modelMaxLimitContoursH,    // modelSizeH
                                           "MaxLimitContour",         // borderTitle
                                           null,                      // tip
                                           "MaxLimitContour.Width",   // tipWidth
                                           "MaxLimitContour.Height"); // tipHeight

        Box box4Limits = Box.createHorizontalBox();
        box4Limits.add(cntrlMinLimit);
        box4Limits.add(Box.createHorizontalStrut(2));
        box4Limits.add(cntrlMaxLimit);

        Component cntrlBindSize  = makeSliderVert(modelBindSize, "Bind size", null);
        Component cntrlBindIndex = makeSliderVert(modelShowBindIndex, "Bind index", null);
        Box box4Binds = Box.createHorizontalBox();
        box4Binds.add(cntrlBindSize);
        box4Binds.add(Box.createHorizontalStrut(2));
        box4Binds.add(cntrlBindIndex);


        addChangeListener("modelMinLimitContoursW", modelMinLimitContoursW , v -> params.minLimitContours.width  = v);
        addChangeListener("modelMinLimitContoursH", modelMinLimitContoursH , v -> params.minLimitContours.height = v);
        addChangeListener("modelMaxLimitContoursW", modelMaxLimitContoursW , v -> params.maxLimitContours.width  = v);
        addChangeListener("modelMaxLimitContoursH", modelMaxLimitContoursH , v -> params.maxLimitContours.height = v);
        addChangeListener("modelBindSize"         , modelBindSize          , v -> params.bindSize                = v);
        addChangeListener("modelShowBindIndex"    , modelShowBindIndex     , v -> params.showBindIndex           = v);

        box4Options.add(box4Limits);
        box4Options.add(Box.createVerticalStrut(2));
        box4Options.add(box4Binds);
        return box4Options;
    }

    @Override
    public BindToNeighborTabParams getParams() {
        return params;
    }

}
