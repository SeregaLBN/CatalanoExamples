package ksn.imgusage.tabs.opencv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.bytedeco.javacpp.Loader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import ksn.imgusage.type.dto.opencv.CascadeClassifierTabParams;
import ksn.imgusage.utils.OpenCvHelper;

/** <a href='https://docs.opencv.org/3.4/db/d28/tutorial_cascade_classifier.html'>Cascade Classifier - Object Detection using Haar feature-based cascade classifiers</a> */
public class CascadeClassifierTab extends OpencvFilterTab<CascadeClassifierTabParams> {

    public static final String TAB_TITLE = "CascadeClassifier";
    public static final String TAB_NAME  = TAB_PREFIX + TAB_TITLE;
    public static final String TAB_DESCRIPTION = "Object Detection using Haar feature-based cascade classifiers";

  //private static final Scalar FUCHSIA = new Scalar(0xFF, 0x00, 0xFF);
    private static final Scalar BLUE    = new Scalar(0xFF, 0x00, 0x00);
    private static final Scalar LIME    = new Scalar(0x00, 0xFF, 0x00);

    /**
     *  Hardcoded resources from
     * @see /home/USER/.gradle/caches/modules-2/files-2.1/org.bytedeco/opencv/4.3.0-1.5.3/fecbcb851829fa5394253011580563001e5aa71c/opencv-4.3.0-1.5.3-sources.jar
     * #/org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_*.xml
     * <br> TODO: get dynamically at runtime
     **/
    public enum EHaarcascade {
            eye_tree_eyeglasses       , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_eye_tree_eyeglasses.xml
            eye                       , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_eye.xml
            frontalcatface_extended   , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalcatface_extended.xml
            frontalcatface            , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalcatface.xml
            frontalface_alt_tree      , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalface_alt_tree.xml
            frontalface_alt           , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalface_alt.xml
            frontalface_alt2          , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalface_alt2.xml
            frontalface_default       , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_frontalface_default.xml
            fullbody                  , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_fullbody.xml
            lefteye_2splits           , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_lefteye_2splits.xml
            licence_plate_rus_16stages, // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_licence_plate_rus_16stages.xml
            lowerbody                 , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_lowerbody.xml
            profileface               , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_profileface.xml
            righteye_2splits          , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_righteye_2splits.xml
            russian_plate_number      , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_russian_plate_number.xml
            smile                     , // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_smile.xml
            upperbody };                // /org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_upperbody.xml
    private static final String HAARCASCADE_PATH_NAME_PREFIX = "org/bytedeco/opencv/linux-x86/share/opencv4/haarcascades/haarcascade_";
    private static final String HAARCASCADE_PATH_NAME_SUFFIX = ".xml";

    private static final EnumMap<EHaarcascade, Path> HAARCASCADES;

    static {
        Path tmpDir;
        try {
            tmpDir = Path.of(System.getProperty("java.io.tmpdir"), "ImageFilterExamples", "CascadeClassifier");
            Files.createDirectories(tmpDir);
        } catch (Exception ex) {
            throw new RuntimeException("Can not create temporaly directory", ex);
        }

        HAARCASCADES = new EnumMap<>(EHaarcascade.class);
        for (EHaarcascade e : EHaarcascade.values()) {
            try {
                File xml = Loader.extractResource(Loader.class, HAARCASCADE_PATH_NAME_PREFIX + e.name() + HAARCASCADE_PATH_NAME_SUFFIX, tmpDir.toFile(), "haarcascades", null);
                if (xml != null)
                    HAARCASCADES.put(e, xml.toPath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private CascadeClassifierTabParams params;
    private CascadeClassifier first;
    private CascadeClassifier second;

    @Override
    public Component makeTab(CascadeClassifierTabParams params) {
        if (params == null)
            params = new CascadeClassifierTabParams();
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
        if (first == null) {
            CascadeClassifier tmp = new CascadeClassifier();
            tmp.load(HAARCASCADES.get(params.first).toString());
            first = tmp;
        }
        if ((second == null) && (params.second != null)) {
            CascadeClassifier tmp = new CascadeClassifier();
            tmp.load(HAARCASCADES.get(params.second).toString());
            second = tmp;
        }

        // cast to gray image
        Mat imageGray = OpenCvHelper.toGray(imageMat);
        Imgproc.equalizeHist(imageGray, imageGray);

        // Detect faces
        MatOfRect faces = new MatOfRect();
        first.detectMultiScale(imageGray, faces);
        List<Rect> listOfFaces = faces.toList();
        for (Rect face : listOfFaces) {
            //Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            //Imgproc.ellipse(imageMat, center, new Size(face.width / 2, face.height / 2), 0, 0, 360, FUCHSIA);
            Imgproc.rectangle(imageMat, face, LIME, 3);

            Mat faceROI = imageGray.submat(face);
            if (second != null) {
                // In each face, detect eyes
                MatOfRect eyes = new MatOfRect();
                second.detectMultiScale(faceROI, eyes);
                List<Rect> listOfEyes = eyes.toList();
                for (Rect eye : listOfEyes) {
                    Point eyeCenter = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
                    int radius = (int) Math.round((eye.width + eye.height) * 0.25);
                    Imgproc.circle(imageMat, eyeCenter, radius, BLUE, 2);
                }
            }
        }
    }

    @Override
    protected Component makeOptions() {
        Box box1 = Box.createHorizontalBox();
        box1.add(Box.createHorizontalStrut(5));
        box1.add(makeComboBox(
                              EHaarcascade.values(),
                              () -> params.first,
                              v  -> {
                                  params.first = v;
                                  first = null;
                              },
                              "params.first",
                              "Haar-cascade first model",
                              "Haar-cascade detection pretrained models"));
        box1.add(Box.createHorizontalStrut(5));


        EHaarcascade[] def = EHaarcascade.values();
        EHaarcascade[] vals = new EHaarcascade[1 + def.length];
        int i = 0;
        vals[i++] = null;
        for (EHaarcascade e : def) {
            vals[i++] = e;
        }

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalStrut(5));
        box2.add(makeComboBox(
                              vals,
                              () -> params.second,
                              v  -> {
                                  params.second = v;
                                  second = null;
                              },
                              "params.second",
                              "Haar-cascade second model",
                              "Haar-cascade detection pretrained models"));
        box2.add(Box.createHorizontalStrut(5));


        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(TAB_TITLE + " options"));
        box.add(Box.createVerticalStrut(5));
        box.add(box1);
        box.add(Box.createVerticalStrut(5));
        box.add(box2);
        box.add(Box.createVerticalStrut(5));


        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new BorderLayout());
//        panelOptions.setBorder(BorderFactory.createTitledBorder(getTitle() + " options"));
        panelOptions.add(box    , BorderLayout.NORTH);

        return panelOptions;
    }

    @Override
    public CascadeClassifierTabParams getParams() {
        return params;
    }

}
