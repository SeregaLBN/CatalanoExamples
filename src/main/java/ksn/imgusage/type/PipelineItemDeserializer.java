package ksn.imgusage.type;

import java.lang.reflect.Type;

import jakarta.json.JsonObject;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import ksn.imgusage.utils.JsonHelper;
import ksn.imgusage.utils.MapperFilter;

public class PipelineItemDeserializer implements JsonbDeserializer<PipelineItem> {

    @Override
    public PipelineItem deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        PipelineItem res = new PipelineItem();

        JsonObject jsonObj = parser.getObject();
        JsonObject params = jsonObj.getJsonObject(PipelineItem.KEY_PARAMS);
        res.tabName = jsonObj.getString(PipelineItem.KEY_TAB_NAME);
        res.params  = JsonHelper.fromJson(params.toString(), MapperFilter.getTabParamsClass(res.tabName));

        return res;
    }

}
