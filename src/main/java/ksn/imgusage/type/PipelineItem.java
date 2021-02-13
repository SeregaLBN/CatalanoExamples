package ksn.imgusage.type;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import ksn.imgusage.tabs.ITabParams;

@JsonbTypeDeserializer(PipelineItemDeserializer.class)
public class PipelineItem {

    public static final String KEY_TAB_NAME = "tabName";
    public static final String KEY_PARAMS   = "params";

    @JsonbProperty(KEY_TAB_NAME)
    public String tabName;

    @JsonbProperty(KEY_PARAMS)
    public ITabParams params;

}
