package cn.hiboot.mcn.core.jackson;

import java.util.List;
import java.util.Map;

/**
 * JsonAttributeConverters
 *
 * @author DingHao
 * @since 2025/2/25 10:42
 */
public final class JsonAttributeConverters {

    public static class ListStringConverter extends JsonAttributeConverter<List<String>> {

        public ListStringConverter() {
            super(getTypeFactory().constructCollectionType(List.class, String.class));
        }

    }

    public static class ListLongConverter extends JsonAttributeConverter<List<Long>> {

        public ListLongConverter() {
            super(getTypeFactory().constructCollectionType(List.class, Long.class));
        }

    }

    public static class ListIntegerConverter extends JsonAttributeConverter<List<Integer>> {

        public ListIntegerConverter() {
            super(getTypeFactory().constructCollectionType(List.class, Integer.class));
        }

    }

    public static class ListMapConverter extends JsonAttributeConverter<List<Map<String, Object>>> {

        public ListMapConverter() {
            super(getTypeFactory().constructCollectionType(List.class, Map.class));
        }

    }

    public static class MapConverter extends JsonAttributeConverter<Map<String, Object>> {

        public MapConverter() {
            super(getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        }

    }

}
