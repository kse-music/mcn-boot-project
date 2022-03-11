package cn.hiboot.mcn.autoconfigure.web.filter.xss;


import cn.hiboot.mcn.autoconfigure.web.filter.FilterProperties;
import cn.hiboot.mcn.core.util.JacksonUtils;
import cn.hiboot.mcn.core.util.McnUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * prevent XSS attack
 *
 * @author DingHao
 * @since 2019/1/9 11:02
 */
public class  XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final FilterProperties filterProperties;

    public XssHttpServletRequestWrapper(HttpServletRequest request, FilterProperties filterProperties) {
        super(request);
        this.filterProperties = filterProperties;
    }

    @Override
    public String getParameter(String name) {
        if(!filterProperties.isFilterRichText() && isRichTextParameterName(name)){
            return super.getParameter(name);
        }
        String value = super.getParameter(cleanParameterName(name));
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = cleanParameterValue(value);
        }
        return value;
    }

    private boolean isRichTextParameterName(String name){
        return "content".equals(name) || name.endsWith("WithHtml");
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] arr = super.getParameterValues(cleanParameterName(name));
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = cleanParameterValue(arr[i]);
            }
        }
        return arr;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                key = cleanParameterName(key);
                String[] values = parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (McnUtils.isNotNullAndEmpty(value)) {
                        value = cleanParameterValue(value);
                    }
                    values[i] = value;
                }
                map.put(key, values);
            }
        }
        return map;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        Map<String, Object> map = JacksonUtils.getObjectMapper().readValue(super.getInputStream(),new TypeReference<Map<String, Object>>(){});

        Map<String, Object> resultMap = new HashMap<>(map.size());

        for (String key : map.keySet()) {
            key = cleanParameterName(key);
            Object value = map.get(key);
            if (value instanceof String) {
                String val = cleanParameterValue(value.toString());
                resultMap.put(key, val);
            } else {
                resultMap.put(key, value);
            }
        }

        String str = JacksonUtils.toJson(resultMap);

        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(str.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return arrayInputStream.read();
            }
        };
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(cleanParameterName(name));
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = cleanParameterValue(value);
        }
        return value;
    }

    private String cleanParameterName(String name){
        if(filterProperties.isFilterParameterName()){
            return HtmlUtils.htmlEscape(name);
        }
        return name;
    }

    private String cleanParameterValue(String value){
        return HtmlUtils.htmlEscape(value);
    }

}
