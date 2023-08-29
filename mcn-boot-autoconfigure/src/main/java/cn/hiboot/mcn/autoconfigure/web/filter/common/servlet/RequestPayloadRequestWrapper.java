package cn.hiboot.mcn.autoconfigure.web.filter.common.servlet;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

/**
 * RequestPayloadRequestWrapper
 *
 * @author DingHao
 * @since 2022/6/8 17:28
 */
public class RequestPayloadRequestWrapper extends HttpServletRequestWrapper {

    private final String data;

    public RequestPayloadRequestWrapper(HttpServletRequest request) {
        super(request);
        this.data = JsonRequestHelper.getData(request);
    }

    public String getPayload() {
        return data;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return JsonRequestHelper.createInputStream(data);
    }
}
