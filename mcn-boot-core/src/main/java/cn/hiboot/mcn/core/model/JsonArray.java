package cn.hiboot.mcn.core.model;

import cn.hiboot.mcn.core.util.JacksonUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * JsonArray
 *
 * @author DingHao
 * @since 2023/3/2 11:14
 */
public class JsonArray extends ArrayNode {

    public JsonArray() {
        this(null);
    }

    public JsonArray(ArrayNode arrayNode) {
        super(new JsonNodeFactory(), jsonNodes(arrayNode));
    }

    static JsonArray of(JsonNode jsonNode){
        return new JsonArray((ArrayNode) jsonNode);
    }

    private static List<JsonNode> jsonNodes(ArrayNode arrayNode) {
        if (arrayNode == null) {
            return new ArrayList<>();
        }
        List<JsonNode> list = new ArrayList<>(arrayNode.size());
        for (JsonNode jsonNode : arrayNode) {
            list.add(jsonNode);
        }
        return list;
    }

    public JsonArray add(JsonObject jsonObject){
        super.add(jsonObject);
        return this;
    }

    public JsonArray set(int index, JsonObject jsonObject){
        super.set(index,jsonObject);
        return this;
    }

    public JsonArray remove(int index){
        super.remove(index);
        return this;
    }

    public JsonArray findAll(Predicate<JsonObject> filter){
        JsonArray jsonArray = new JsonArray();
        for (JsonNode jsonNode : this) {
            if(filter.test(JsonObject.of(jsonNode))){
                jsonArray.add(jsonNode);
            }
        }
        return jsonArray;
    }

    public <T> List<T> toList(){
        List<T> result = new ArrayList<>();
        int size = this.size();
        for (int i = 0; i < size; i++) {
            result.add(value(i));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T value(int index) {
        JsonNode jsonNode = get(index);
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isObject()) {
            return (T) JsonObject.of(jsonNode);
        }
        if (jsonNode.isArray()) {
            return (T) JsonArray.of(jsonNode);
        }
        if (jsonNode.isNumber()) {
            return (T) jsonNode.numberValue();
        }
        if (jsonNode.isBoolean()) {
            return (T) Boolean.valueOf(jsonNode.booleanValue());
        }
        if (jsonNode.isTextual()) {
            return (T) jsonNode.textValue();
        }
        return (T) jsonNode;
    }

    public Iterable<JsonObject> asJsonObject() {
        Iterator<JsonNode> iterator = iterator();
        return () -> new Iterator<>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public JsonObject next() {
                return JsonObject.of(iterator.next());
            }

        };
    }

    public static JsonArray fromList(List<?> list) {
        return new JsonArray(JacksonUtils.getObjectMapper().valueToTree(list));
    }

    public static class JsonArrayDeserializer extends StdDeserializer<JsonArray> {

        public JsonArrayDeserializer() {
            super(JsonArray.class);
        }

        @Override
        public JsonArray deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            return new JsonArray(p.readValueAsTree());
        }

    }

}
