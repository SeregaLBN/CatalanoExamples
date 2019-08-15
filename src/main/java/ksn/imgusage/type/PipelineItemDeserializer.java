package ksn.imgusage.type;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ksn.imgusage.utils.JsonHelper;
import ksn.imgusage.utils.MapperFilter;

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
        res.params  = JsonHelper.fromJson(node.get(PipelineItem.KEY_PARAMS), MapperFilter.getTabParamsClass(res.tabName));

        return res;
    }

}
