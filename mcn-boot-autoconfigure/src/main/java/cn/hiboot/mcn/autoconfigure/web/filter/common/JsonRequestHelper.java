package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JsonRequestHelper
 *
 * @author DingHao
 * @since 2022/8/19 16:45
 */
public interface JsonRequestHelper {

    static String getData(HttpServletRequest request){
        try{
            return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        }catch (IOException e){
            return "";
        }
    }

    static boolean isJsonRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if(header == null){
            return false;
        }
        return header.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    static ServletInputStream createInputStream(String data) {
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
}
