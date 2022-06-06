package cn.hiboot.mcn.autoconfigure.web.filter.integrity;


import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * DataIntegrityRequestWrapper
 *
 * @author DingHao
 * @since 2022/6/4 23:41
 */
public class DataIntegrityRequestWrapper extends HttpServletRequestWrapper {

   private final String data;

    public DataIntegrityRequestWrapper(HttpServletRequest request,String data) {
        super(request);
        this.data = data;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
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