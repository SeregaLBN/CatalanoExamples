package ksn.imgusage.type.dto;

import java.io.File;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.type.Padding;
import ksn.imgusage.type.Size;

/** Init parameters for {@link FirstTab} */
public class FirstTabParams implements ITabParams {

    /** source image */
    @JsonIgnore
    public File    imageFile;

    public boolean useGray;
    public boolean useScale;
    public Size    keepToSize;
    public boolean useKeepAspectRatio;

    /** padding of Region Of Interest */
    public Padding boundOfRoi;

    public FirstTabParams() {}

    public FirstTabParams(
        File    imageFile,
        boolean useGray,
        boolean useScale,
        Size    keepToSize,
        boolean useKeepAspectRatio,
        Padding boundOfRoi)
    {
        this.imageFile          = imageFile;
        this.useGray            = useGray;
        this.useScale           = useScale;
        this.keepToSize         = keepToSize;
        this.useKeepAspectRatio = useKeepAspectRatio;
        this.boundOfRoi         = boundOfRoi;
    }

    @JsonProperty("imageFile")
    public String getImagePath() {
        if (imageFile == null)
            return null;
        return imageFile.getPath();
    }
    @JsonProperty("imageFile")
    public void setImagePath(String imagePath) {
        if (imagePath == null)
            this.imageFile = null;
        else
            this.imageFile = Paths.get(imagePath).toFile();
    }

    @Override
    public String toString() {
        return String.format(
                "{imageFile='%s', useGray=%b, useScale=%b, keepToSize=%s, useKeepAspectRatio=%b, boundOfRoi=%s}",
                imageFile,
                useGray,
                useScale,
                keepToSize,
                useKeepAspectRatio,
                boundOfRoi);
    }

}
