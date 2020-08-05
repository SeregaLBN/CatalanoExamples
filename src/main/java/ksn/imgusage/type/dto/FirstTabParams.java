package ksn.imgusage.type.dto;

import java.io.File;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ksn.imgusage.tabs.FirstTab;
import ksn.imgusage.tabs.ITabParams;

/** Init parameters for {@link FirstTab} */
public class FirstTabParams implements ITabParams {

    public enum EFileType { IMAGE, VIDEO }

    /** source image */
    @JsonIgnore
    public File    imageFile;

    public EFileType fileType = EFileType.IMAGE;

    public boolean useScale;

    public FirstTabParams() {}

    public FirstTabParams(
        File    imageFile,
        EFileType fileType,
        boolean useScale)
    {
        this.imageFile = imageFile;
        this.fileType  = fileType;
        this.useScale  = useScale;
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
                "{imageFile='%s', fileType=%s, useScale=%b}",
                imageFile,
                fileType,
                useScale);
    }

}
