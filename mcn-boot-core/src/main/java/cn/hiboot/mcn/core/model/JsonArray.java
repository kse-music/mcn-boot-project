package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.databind.JsonNode;

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
public class JsonArray implements Iterable<JsonObject> {
    private final List<JsonObject> list;

    public JsonArray(){
        list = new ArrayList<>();
    }

    public JsonArray(JsonNode jsonNode) {
        this();
        for (JsonNode node : jsonNode) {
            list.add(new JsonObject(node));
        }
    }

    public JsonArray add(JsonObject jsonObject){
        list.add(jsonObject);
        return this;
    }

    public JsonArray add(int index,JsonObject jsonObject){
        list.add(index,jsonObject);
        return this;
    }

    public JsonArray remove(JsonObject jsonObject){
        list.remove(jsonObject);
        return this;
    }

    public JsonArray remove(int index){
        list.remove(index);
        return this;
    }

    public JsonArray removeIf(Predicate<JsonObject> filter){
        list.removeIf(filter);
        return this;
    }

    public JsonObject findFirst(Predicate<JsonObject> filter){
        return list.stream().filter(filter).findFirst().orElse(null);
    }

    public JsonArray findAll(Predicate<JsonObject> filter){
        JsonArray jsonArray = new JsonArray();
        list.stream().filter(filter).forEach(jsonArray::add);
        return jsonArray;
    }

    public JsonObject get(int index){
        return list.get(index);
    }

    @Override
    public Iterator<JsonObject> iterator() {
        return list.iterator();
    }

    public int size(){
        return list.size();
    }

}
