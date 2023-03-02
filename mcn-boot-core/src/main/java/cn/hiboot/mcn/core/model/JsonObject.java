package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.databind.JsonNode;

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
        return jsonNode().intValue();
    }
    
    public int getInt(String field){
        return jsonNode().at(toJsonExpr(field)).intValue();
    }

    public long getLong(){
        return jsonNode().longValue();
    }

    public long getLong(String field){
        return jsonNode().at(toJsonExpr(field)).longValue();
    }

    public double getDouble(){
        return jsonNode().doubleValue();
    }
    
    public double getDouble(String field){
        return jsonNode().at(toJsonExpr(field)).doubleValue();
    }

    public short getShort(){
        return jsonNode().shortValue();
    }
    
    public short getShort(String field){
        return jsonNode().at(toJsonExpr(field)).shortValue();
    }

    public String getString(){
        return jsonNode().asText();
    }
    
    public String getString(String field){
        return jsonNode().at(toJsonExpr(field)).asText();
    }

    public boolean getBoolean(){
        return jsonNode().booleanValue();
    }
    
    public boolean getBoolean(String field){
        return jsonNode().at(toJsonExpr(field)).booleanValue();
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T readNumber(){
        return (T) jsonNode().numberValue();
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T readNumber(String field){
        return (T) jsonNode().at(toJsonExpr(field)).numberValue();
    }

    public JsonObject getJsonObject(String field){
        return new JsonObject(jsonNode().with(field));
    }

    public JsonArray getJsonArray(String field){
        return new JsonArray(jsonNode().withArray(field));
    }

    private String toJsonExpr(String field){
        return SLASH + field;
    }
    
    private JsonNode jsonNode(){
       return jsonNode;
    }
    
}
