package cn.hiboot.mcn.core.model;

import cn.hiboot.mcn.core.util.JacksonUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.function.Function;

/**
 * JsonObject
 *
 * @author DingHao
 * @since 2023/3/2 11:14
 */
public class JsonObject extends ObjectNode {

    private static final String SLASH = "/";

    public JsonObject() {
        super(new JsonNodeFactory());
    }

    public JsonObject(ObjectNode objectNode) {
        this();
        if (objectNode != null) {
            setAll(objectNode);
        }
    }

    static JsonObject of(JsonNode jsonNode) {
        return new JsonObject((ObjectNode) jsonNode);
    }

    public Integer getInteger(String field) {
        return getValue(field, JsonNode::asInt);
    }

    public Long getLong(String field) {
        return getValue(field, JsonNode::asLong);
    }

    public Double getDouble(String field) {
        return getValue(field, JsonNode::asDouble);
    }

    public String getString(String field) {
        return getValue(field, JsonNode::asText);
    }

    public Boolean getBoolean(String field) {
        return getValue(field, JsonNode::booleanValue);
    }

    public boolean contains(String field) {
        return !nextNode(field).isMissingNode();
    }

    public JsonObject getJsonObject(String field) {
        return getValue(field, JsonObject::of);
    }

    public JsonArray getJsonArray(String field) {
        return getValue(field, JsonArray::of);
    }

    private <R> R getValue(String field, Function<JsonNode, R> function) {
        JsonNode nextNode = nextNode(field);
        if (nextNode.isMissingNode() || nextNode.isNull()) {
            return null;
        }
        return function.apply(nextNode);
    }

    private JsonNode nextNode(String field) {
        return at(SLASH + field);
    }

    public Map<String, Object> toMap() {
        return JacksonUtils.toMap(this);
    }

    public static JsonObject fromMap(Map<String, Object> map) {
        return new JsonObject(JacksonUtils.getObjectMapper().valueToTree(map));
    }

    public static class JsonObjectDeserializer extends ValueDeserializer<JsonObject> {

        @Override
        public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            return new JsonObject(p.readValueAsTree());
        }

    }

}
