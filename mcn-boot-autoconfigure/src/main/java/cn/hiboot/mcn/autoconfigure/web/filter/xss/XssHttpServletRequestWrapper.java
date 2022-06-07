package cn.hiboot.mcn.autoconfigure.web.filter.xss;


import cn.hiboot.mcn.core.util.McnUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * prevent XSS attack
 *
 * @author DingHao
 * @since 2019/1/9 11:02
 */
public class  XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final XssProperties xssProperties;
    private final XssProcessor xssProcessor;

    public XssHttpServletRequestWrapper(HttpServletRequest request, XssProperties xssProperties,XssProcessor xssProcessor) {
        super(request);
        this.xssProperties = xssProperties;
        this.xssProcessor = xssProcessor;
    }

    @Override
    public String getParameter(String name) {
        if(!xssProperties.isFilterRichText() && isRichTextParameterName(name)){
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
    public String getHeader(String name) {
        String value = super.getHeader(cleanParameterName(name));
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = cleanParameterValue(value);
        }
        return value;
    }

    private String cleanParameterName(String name){
        if(xssProperties.isFilterParameterName()){
            return xssProcessor.process(name);
        }
        return name;
    }

    private String cleanParameterValue(String value){
        return xssProcessor.process(value);
    }

}
