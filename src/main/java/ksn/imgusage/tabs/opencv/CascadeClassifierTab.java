package ksn.imgusage.tabs.opencv;

import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;

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

    private static final Scalar FUCHSIA = new Scalar(0xFF, 0x00, 0xFF);
    private static final Scalar BLUE    = new Scalar(0xFF, 0x00, 0x00);

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
            tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "ImageFilterExamples", "CascadeClassifier");
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
            Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            Imgproc.ellipse(imageMat, center, new Size(face.width / 2, face.height / 2), 0, 0, 360, FUCHSIA);

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
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(TAB_TITLE + " options"));

        box.add(Box.createVerticalGlue());

        {
            Component cntrlMorphOper = makeComboBox(
                         EHaarcascade.values(),
                         () -> params.first,
                         v  -> {
                             params.first = v;
                             first = null;
                         },
                         "params.first",
                         "Haar-cascade first model",
                         "Haar-cascade detection pretrained models");
            box.add(cntrlMorphOper);
        }

        box.add(Box.createVerticalGlue());

        {
            EHaarcascade[] def = EHaarcascade.values();
            EHaarcascade[] vals = new EHaarcascade[1 + def.length];
            int i = 0;
            vals[i++] = null;
            for (EHaarcascade e : def) {
                vals[i++] = e;
            }

            Component cntrlMorphOper = makeComboBox(
                         vals,
                         () -> params.second,
                         v  -> {
                             params.second = v;
                             second = null;
                         },
                         "params.second",
                         "Haar-cascade second model",
                         "Haar-cascade detection pretrained models");
            box.add(cntrlMorphOper);
        }

        box.add(Box.createVerticalGlue());
        return box;
    }

    @Override
    public CascadeClassifierTabParams getParams() {
        return params;
    }

    /** /
    public static void main(String[] args) throws IOException {
        CodeSource src = org.bytedeco.javacpp.BooleanPointer.class.getProtectionDomain().getCodeSource();
//        CodeSource src = CascadeClassifier.class.getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<>();

        if (src != null) {
            URL jar = src.getLocation();
            try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                ZipEntry ze = null;
                while ((ze = zip.getNextEntry()) != null) {
                    String entryName = ze.getName();
//  if( entryName.startsWith("images") && entryName.endsWith(".png") ) {
                    if (entryName.endsWith("/") || entryName.endsWith(".class"))
                        continue;
                    list.add(entryName);
// }
                }
            }
        }
        System.err.println("xx");
        list.forEach(x -> System.out.println(x));
    }
    /** /
    public static void main(String[] args) throws IOException {
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader().getParent();
//        ClassLoader classLoader = org.bytedeco.javacpp.BooleanPointer.class.getProtectionDomain().getClassLoader();
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader classLoader = CascadeClassifier.class.getClassLoader();
//        ClassLoader classLoader = CascadeClassifier.class.getProtectionDomain().getClassLoader();
        Enumeration<URL> roots = classLoader.getResources("");
//        Enumeration<URL> roots = ClassLoader.getSystemResources("");
        List<URL> allRoots = new ArrayList<>();
        while (roots.hasMoreElements()) {
            URL url = roots.nextElement();
            allRoots.add(url);
        }
        URLClassLoader urlClassLoader = new URLClassLoader(allRoots.toArray(new URL[allRoots.size()]));
        ClasspathFileListPrinter pr = new ClasspathFileListPrinter(urlClassLoader);
        pr.print();
    }
    /** /
    private static Iterator list(ClassLoader CL) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class CL_class = CL.getClass();
        while (CL_class != java.lang.ClassLoader.class) {
            CL_class = CL_class.getSuperclass();
        }
        java.lang.reflect.Field ClassLoader_classes_field = CL_class.getDeclaredField("classes");
        ClassLoader_classes_field.setAccessible(true);
        Vector classes = (Vector) ClassLoader_classes_field.get(CL);
        return classes.iterator();
    }

    public static void main(String args[]) throws Exception {
        ClassLoader myCL = Thread.currentThread().getContextClassLoader();
        while (myCL != null) {
            System.out.println("ClassLoader: " + myCL);
            for (Iterator iter = list(myCL); iter.hasNext();) {
                System.out.println("\t" + iter.next());
            }
            myCL = myCL.getParent();
        }
    }
    /** /
    public static class InstrumentHook {

        public static void premain(String agentArgs, Instrumentation inst) {
            if (agentArgs != null) {
                System.getProperties().put(AGENT_ARGS_KEY, agentArgs);
            }
            System.getProperties().put(INSTRUMENTATION_KEY, inst);
        }

        public static Instrumentation getInstrumentation() {
            return (Instrumentation) System.getProperties().get(INSTRUMENTATION_KEY);
        }

        // Needn't be a UUID - can be a String or any other object that implements equals().
        private static final Object AGENT_ARGS_KEY = UUID.fromString("887b43f3-c742-4b87-978d-70d2db74e40e");

        private static final Object INSTRUMENTATION_KEY = UUID.fromString("214ac54a-60a5-417e-b3b8-772e80a16667");

    }

    public static void main(String[] args) {
        Instrumentation inst = InstrumentHook.getInstrumentation();
        for (Class<?> clazz: inst.getAllLoadedClasses()) {
            System.err.println(clazz.getName());
        }
    }

    /**/

}
