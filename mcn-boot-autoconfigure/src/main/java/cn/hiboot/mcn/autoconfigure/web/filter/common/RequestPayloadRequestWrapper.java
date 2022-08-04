package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * RequestPayloadRequestWrapper
 *
 * @author DingHao
 * @since 2022/6/8 17:28
 */
public class RequestPayloadRequestWrapper extends HttpServletRequestWrapper {

    private String data;

    public RequestPayloadRequestWrapper(HttpServletRequest request) {
        super(request);
        if(isJsonRequest(request)){
            this.data = getData(request);
        }
    }

    public RequestPayloadRequestWrapper(HttpServletRequest request,String data) {
        super(request);
        this.data = data;
    }

    public static String getData(HttpServletRequest request){
        try{
            return StreamUtils.copyToString(request.getInputStream(),StandardCharsets.UTF_8);
        }catch (IOException e){
            return "";
        }
    }

    public static boolean isJsonRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if(header == null){
            return false;
        }
        return header.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    public static ServletInputStream createInputStream(String data) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
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
    public ServletInputStream getInputStream() throws IOException {
        if(isJsonRequest((HttpServletRequest)getRequest())){
            return createInputStream(data);
        }
        return super.getInputStream();
    }
}
