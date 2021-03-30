package ksn.imgusage.type.dto.opencv;

import java.util.Locale;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.MserTab;
import ksn.imgusage.type.Size;

/** Init parameters for {@link MserTab} */
public class MserTabParams implements ITabParams {

    /** it compares (size_i − size_iDelta)/size_iDelta */
    public int     delta = 5;
    public double  maxVariation = 0.25;
    /** for color image, trace back to cut off mser with diversity less than min_diversity */
    public double  minDiversity = .2;
    /** for color image, the evolution steps */
    public int     maxEvolution = 200;
    /** for color image, the area threshold to cause re-initialize */
    public double  areaThreshold = 1.01;
    /** for color image, ignore too small margin */
    public double  minMargin = 0.003;
    /** for color image, the aperture size for edge blur */
    public int     edgeBlurSize = 5;

    /** Additional restrictions on the minimum symbol size */
    public Size    minSymbol = new Size(3, 5);
    /** Additional restrictions on the maximum symbol size */
    public Size    maxSymbol = new Size(30, 45);

    /** Minimum line heigth */
    public int    minLineHeight = 10;
   //** Maximum line heigth */
  //public int    maxLineHeight => maxSymbol.height * 2.3;

    /** the number of characters stuck together */
    public int stuckSymbols = 1;

    /** Show on source */
    public boolean showOnSource = true;
    /** Show inner contours */
    public boolean showInner = true;
    /** Invert regions */
    public boolean invert;

    /** Merge small regions (by vertically) into one symbol */
    public boolean mergeRegionsVertically = true;
    /** Merge small regions (by horizontally) into one symbol */
    public boolean mergeRegionsHorizontally = true;
    /** Fit symbol height to word height */
    public boolean fitSymbolHeight = true;

    /** Show rectangle of regions (mark single char) */
    public boolean markChars;
    /** Show groups of regions as a word (mark word) */
    public boolean markWords;
    /** Show word groups as a line */
    public boolean markLines;

    /** Width coefficient between symbols. Determines the distance at which characters are combined into words */
    public double wordWidthCoef = 0.35;
    /** Width coefficient between words. Determines the distance at which words are combined into a line */
    public double lineWidthCoef = 0.9;


    @Override
    public String toString() {
        return String.format(Locale.US,
            "{ delta=%d, maxVariation=%.2f, minDiversity=%.2f, maxEvolution=%d, areaThreshold=%.2f, minMargin=%.2f, edgeBlurSize=%d"
            + ", minSymbol=%s, maxSymbol=%s, minLineHeight=%s, stuckSymbols=%d"
            + ", wordWidthCoef=%.2f, lineWidthCoef=%.2f"
            + ", mergeRegionsVertivally=%b, mergeRegionsHorizontally=%b, fitSymbolHeight=%b"
            + ", showOnSource=%b, invert=%b, showInner=%b, markChars=%b, markWords=%b, markLines=%b }",
            delta,
            maxVariation,
            minDiversity,
            maxEvolution,
            areaThreshold,
            minMargin,
            edgeBlurSize,
            minSymbol,
            maxSymbol,
            minLineHeight,
            stuckSymbols,
            wordWidthCoef, lineWidthCoef,
            mergeRegionsVertically, mergeRegionsHorizontally,
            fitSymbolHeight,
            showOnSource,
            invert,
            showInner,
            markChars,
            markWords,
            markLines);
    }

    public static int onlyOdd(int value, int prevValue) {
        return CannyTabParams.onlyOdd(value, prevValue);
    }

}
