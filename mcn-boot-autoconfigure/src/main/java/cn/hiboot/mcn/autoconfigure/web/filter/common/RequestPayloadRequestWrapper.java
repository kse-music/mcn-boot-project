package cn.hiboot.mcn.autoconfigure.web.filter.common;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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
