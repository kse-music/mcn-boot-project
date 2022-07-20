package cn.hiboot.mcn.autoconfigure.web.filter.common;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NameValueProcessorRequestWrapper
 *
 * @author DingHao
 * @since 2019/1/9 11:02
 */
public class NameValueProcessorRequestWrapper extends HttpServletRequestWrapper {

    private List<String> excludeFields;
    private boolean filterParameterName;
    private boolean filterHeaderValue;
    private final NameValueProcessor valueProcessor;

    public NameValueProcessorRequestWrapper(HttpServletRequest request, NameValueProcessor valueProcessor) {
        super(request);
        this.valueProcessor = valueProcessor;
    }

    public NameValueProcessorRequestWrapper excludeFields(List<String> excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    public NameValueProcessorRequestWrapper filterParameterName(boolean filterParameterName) {
        this.filterParameterName = filterParameterName;
        return this;
    }

    public NameValueProcessorRequestWrapper filterHeaderValue(boolean filterHeaderValue) {
        this.filterHeaderValue = filterHeaderValue;
        return this;
    }

    @Override
    public String getParameter(String name) {
        if(isExcludeParameter(name)){
            return super.getParameter(name);
        }
        String value = super.getParameter(cleanParameterName(name));
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = cleanParameterValue(name,value);
        }
        return value;
    }

    private boolean isExcludeParameter(String name){
        if(CollectionUtils.isEmpty(excludeFields)){
            return false;
        }
        return excludeFields.contains(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        if(isExcludeParameter(name)){
            return super.getParameterValues(name);
        }
        String[] arr = super.getParameterValues(cleanParameterName(name));
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = cleanParameterValue(name,arr[i]);
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
                String currentKey = key;
                key = cleanParameterName(currentKey);
                String[] values = parameters.get(currentKey);
                if(isExcludeParameter(currentKey)){
                    map.put(currentKey, values);
                    continue;
                }
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (McnUtils.isNotNullAndEmpty(value)) {
                        value = cleanParameterValue(currentKey,value);
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
        if (filterHeaderValue && McnUtils.isNotNullAndEmpty(value)) {
            value = cleanParameterValue(name,value);
        }
        return value;
    }

    private String cleanParameterName(String name){
        if(filterParameterName){
            return clean(name,name);
        }
        return name;
    }

    private String cleanParameterValue(String name,String value){
        return clean(name,value);
    }

    public String clean(String name,String text){
        return valueProcessor.process(name,text);
    }
}
