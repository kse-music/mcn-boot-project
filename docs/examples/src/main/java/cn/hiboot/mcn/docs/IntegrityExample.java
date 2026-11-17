package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.core.tuples.Triplet;

import java.util.Map;

public class IntegrityExample {
    public static Map<String, String> headers(Map<String, Object> query, String rawJson) {
        Triplet<String, String, String> result = DataIntegrityUtils.signature(query, rawJson);
        return Map.of("TSM", result.getValue0(),
                "nonceStr", result.getValue1(), "signature", result.getValue2());
    }
}
