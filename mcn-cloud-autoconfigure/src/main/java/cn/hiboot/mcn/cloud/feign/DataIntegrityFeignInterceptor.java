package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.core.tuples.Triplet;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DataIntegrityFeignInterceptor
 *
 * @author DingHao
 * @since 2022/6/21 13:45
 */
@ConditionalOnProperty(prefix = "data.integrity.interceptor",name = "enable",havingValue = "true")
public class DataIntegrityFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Map<String, Object> params = new HashMap<>();
        template.queries().forEach((k,v) -> {
            for (String value : v) {
                params.put(k,value);
                break;
            }
        });
        String data = null;
        Collection<String> ct = template.headers().get(HttpHeaders.CONTENT_TYPE);
        if(ct != null && MediaType.APPLICATION_JSON_VALUE.equals(new ArrayList<>(ct).get(0))){
            data = new String(template.body(), StandardCharsets.UTF_8);
        }
        Triplet<String, String, String> triplet = DataIntegrityUtils.signature(params, data);
        template.header("TSM", triplet.getValue0());
        template.header("nonceStr", triplet.getValue1());
        template.header("signature", triplet.getValue2());
    }

}
