package cn.hiboot.mcn.core.model;

import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Function;

/**
 * JsonObject
 *
 * @author DingHao
 * @since 2023/3/2 11:14
 */
public class JsonObject {
    private static final String SLASH = "/";
    
    private final JsonNode jsonNode;

    public JsonObject(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }
    
    public int getInt(){
        return getValue(JsonNode::intValue);
    }
    
    public Integer getInt(String field){
        return getValue(field, JsonNode::intValue);
    }

    public long getLong(){
        return getValue(JsonNode::longValue);
    }

    public Long getLong(String field){
        return getValue(field, JsonNode::longValue);
    }

    public double getDouble(){
        return getValue(JsonNode::doubleValue);
    }
    
    public Double getDouble(String field){
        return getValue(field, JsonNode::doubleValue);
    }

    public short getShort(){
        return getValue(JsonNode::shortValue);
    }
    
    public Short getShort(String field){
        return getValue(field, JsonNode::shortValue);
    }

    public String getString(){
        return getValue(JsonNode::asText);
    }
    
    public String getString(String field){
        return getValue(field, JsonNode::asText);
    }

    public boolean getBoolean(){
        return getValue(JsonNode::booleanValue);
    }
    
    public Boolean getBoolean(String field){
        return getValue(field, JsonNode::booleanValue);
    }

    public Number readNumber(){
        return getValue(JsonNode::numberValue);
    }

    public Number readNumber(String field){
        return getValue(field, JsonNode::numberValue);
    }


    public JsonObject getJsonObject(){
        return new JsonObject(currentNode());
    }

    public JsonArray getJsonArray(){
        return new JsonArray(currentNode());
    }

    public JsonObject getJsonObject(String field){
        return new JsonObject(currentNode().with(field));
    }

    public JsonArray getJsonArray(String field){
        return new JsonArray(currentNode().withArray(field));
    }

    private <R> R getValue(String field, Function<JsonNode,R> function){
        JsonNode node = currentNode().at(SLASH + field);
        if(node.isMissingNode()){
            return null;
        }
        return getValue(node,function);
    }

    private <R> R getValue(Function<JsonNode,R> function){
        return getValue(currentNode(),function);
    }

    private <R> R getValue(JsonNode jsonNode,Function<JsonNode,R> function){
        if(jsonNode.isValueNode()){
            return function.apply(jsonNode);
        }
        throw ServiceException.newInstance("当前节点不是值节点");
    }

    private JsonNode currentNode(){
        return jsonNode;
    }

}
