package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public class ResponseExamples {

    public static RestResp<List<String>> page() {
        return RestResp.ok(List.of("demo"), 1L);
    }

    public static List<String> readNames(String json) {
        return JacksonUtils.toBean(json, new TypeReference<List<String>>() {});
    }

    public static String writeResponse() {
        return JacksonUtils.toJson(page());
    }
}
