package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JsonArray
 *
 * @author DingHao
 * @since 2023/3/2 11:14
 */
public class JsonArray implements Iterable<JsonObject> {
    private final List<JsonObject> list;

    public JsonArray(JsonNode jsonNode) {
        list = new ArrayList<>(jsonNode.size());
        for (JsonNode node : jsonNode) {
            list.add(new JsonObject(node));
        }
    }

    public JsonObject getJsonObject(int index){
        return list.get(index);
    }

    @Override
    public Iterator<JsonObject> iterator() {
        return list.iterator();
    }

}
