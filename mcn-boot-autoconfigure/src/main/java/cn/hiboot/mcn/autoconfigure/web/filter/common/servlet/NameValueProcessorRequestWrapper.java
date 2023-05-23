package cn.hiboot.mcn.autoconfigure.web.filter.common.servlet;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NameValueProcessorRequestWrapper
 *
 * @author DingHao
 * @since 2019/1/9 11:02
 */
class NameValueProcessorRequestWrapper extends HttpServletRequestWrapper {

    private List<String> excludeFields;
    private boolean filterParameterName;
    private boolean processPayload;
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

    public NameValueProcessorRequestWrapper processPayload(boolean processPayload) {
        this.processPayload = processPayload;
        return this;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if(processPayload){
            String data = JsonRequestHelper.getData((HttpServletRequest) getRequest());
            data = clean(null,data);
            return JsonRequestHelper.createInputStream(data);
        }
        return super.getInputStream();
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

    private String clean(String name,String text){
        return valueProcessor.process(name,text);
    }
}
