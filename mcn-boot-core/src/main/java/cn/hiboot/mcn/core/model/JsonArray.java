package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Iterator;

/**
 * JsonArray
 *
 * @author DingHao
 * @since 2023/3/2 11:14
 */
public class JsonArray implements Iterable<JsonObject> {

    private final ArrayNode arrayNode;

    public JsonArray(JsonNode jsonNode) {
        this.arrayNode = (ArrayNode) jsonNode;
    }

    public JsonObject getJsonObject(int index){
        return new JsonObject(arrayNode.get(index));
    }

    public JsonObject getJsonObject(String field){
        return new JsonObject(arrayNode.get(field));
    }

    @Override
    public Iterator<JsonObject> iterator() {
        Iterator<JsonNode> iterator = arrayNode.iterator();
        return new Iterator<JsonObject>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public JsonObject next() {
                return new JsonObject(iterator.next());
            }
        };
    }
}
