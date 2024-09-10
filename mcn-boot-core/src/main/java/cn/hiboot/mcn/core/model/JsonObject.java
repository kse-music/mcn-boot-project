package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        super(new JsonNodeFactory(false));
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
        return of(nextNode(field));
    }

    public JsonArray getJsonArray(String field) {
        return JsonArray.of(nextNode(field));
    }

    private <R> R getValue(String field, Function<JsonNode, R> function) {
        JsonNode nextNode = nextNode(field);
        if (nextNode.isMissingNode()) {
            return null;
        }
        if (nextNode.isValueNode()) {
            return function.apply(nextNode);
        }
        throw new IllegalArgumentException("current node is not value node");
    }

    private JsonNode nextNode(String field) {
        return at(SLASH + field);
    }

}
