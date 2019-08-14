package ksn.imgusage.type;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.utils.MapFilterToTab;

public class PipelineItemDeserializer extends StdDeserializer<PipelineItem> {

    private static final long serialVersionUID = 1;

    public PipelineItemDeserializer() {
        this(PipelineItem.class);
    }

    public PipelineItemDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PipelineItem deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        PipelineItem res = new PipelineItem();

        JsonNode node = jp.getCodec().readTree(jp);
        res.tabName = node.get(PipelineItem.KEY_TAB_NAME).textValue();
        res.pos     = node.get(PipelineItem.KEY_POS).numberValue().intValue();

        JsonNode nParams = node.get(PipelineItem.KEY_PARAMS);
        ObjectMapper objectMapper = new ObjectMapper();
        Class<? extends ITabParams> tabParamsClass = MapFilterToTab.getTabParamsClass(res.tabName);
        ITabParams tabParams = objectMapper.readerFor(tabParamsClass).readValue(nParams);

        res.params = tabParams;

        return res;
    }

}
