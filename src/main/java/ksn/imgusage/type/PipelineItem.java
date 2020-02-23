package ksn.imgusage.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ksn.imgusage.tabs.ITabParams;

@JsonDeserialize(using = PipelineItemDeserializer.class)
public class PipelineItem {

    public static final String KEY_TAB_NAME = "tabName";
    public static final String KEY_PARAMS   = "params";

    @JsonProperty(KEY_TAB_NAME)
    public String tabName;

    @JsonProperty(KEY_PARAMS)
    public ITabParams params;

}
