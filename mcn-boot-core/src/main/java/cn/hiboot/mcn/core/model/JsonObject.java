package cn.hiboot.mcn.core.model;

import cn.hiboot.mcn.core.exception.ServiceException;
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
    
    public JsonObject(ObjectNode objectNode) {
        super(new JsonNodeFactory(false));
        setAll(objectNode);
    }

    static JsonObject of(JsonNode jsonNode) {
        return new JsonObject((ObjectNode) jsonNode);
    }

    public Integer getInt(String field){
        return getValue(field, JsonNode::asInt);
    }

    public Long getLong(String field){
        return getValue(field, JsonNode::asLong);
    }

    public Double getDouble(String field){
        return getValue(field, JsonNode::asDouble);
    }

    public String getString(String field){
        return getValue(field, JsonNode::asText);
    }

    public Boolean getBoolean(String field){
        return getValue(field, JsonNode::booleanValue);
    }

    public boolean contains(String field){
        return !nextNode(field).isMissingNode();
    }

    public JsonObject getJsonObject(String field){
        return of(nextNode(field));
    }

    public JsonArray getJsonArray(String field){
        return JsonArray.of(nextNode(field));
    }

    private <R> R getValue(String field, Function<JsonNode,R> function){
        JsonNode node = nextNode(field);
        if(node.isMissingNode()){
            return null;
        }
        return getValue(node,function);
    }

    private <R> R getValue(JsonNode jsonNode, Function<JsonNode,R> function){
        if(jsonNode.isValueNode()){
            return function.apply(jsonNode);
        }
        throw ServiceException.newInstance("当前节点不是值节点");
    }

    private ObjectNode currentNode(){
        return this;
    }

    private JsonNode nextNode(String field){
        return currentNode().at(SLASH + field);
    }

}
