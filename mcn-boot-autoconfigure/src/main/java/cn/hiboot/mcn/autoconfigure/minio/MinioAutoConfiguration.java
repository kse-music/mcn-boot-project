package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.MinioClient;
import io.minio.http.HttpUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Minio文件上传
 *
 * @author DingHao
 * @since 2021/6/28 22:02
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    private final MinioProperties config;
    private final ObjectProvider<MinioClientBuilderCustomizer> customizers;

    public MinioAutoConfiguration(MinioProperties config, ObjectProvider<MinioClientBuilderCustomizer> customizers) {
        this.config = config;
        this.customizers = customizers;
    }

    @Bean
    public MinioClient minioClient(){
        MinioClient.Builder builder = MinioClient.builder();
        for (MinioClientBuilderCustomizer customizer : customizers) {
            customizer.customize(builder);
        }
        return builder
                .credentials(config.getAccessKey(), config.getSecretKey())
                .endpoint(config.getEndpoint())
                .httpClient(HttpUtils.newDefaultHttpClient(config.getConnectTimeout().toMillis(),config.getWriteTimeout().toMillis(),config.getReadTimeout().toMillis()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(Minio.class)
    public Minio minio(MinioClient minioClient){
        return new DefaultMinio(minioClient,config);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnBean(DispatcherServlet.class)
    private static class PreviewImage{

        @Autowired
        private Minio minio;

        @Bean
        public SimpleUrlHandlerMapping simpleUrlHandlerMapping(){
            return new SimpleUrlHandlerMapping(Collections.singletonMap("_imagePreview",new PreviewImageRequestHandler(minio)), Ordered.LOWEST_PRECEDENCE - 2);
        }

        private static class PreviewImageRequestHandler implements HttpRequestHandler {
            private final Minio minio;

            public PreviewImageRequestHandler(Minio minio) {
                this.minio = minio;
            }

            @Override
            public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                OutputStream os = null;
                try {
                    BufferedImage image = ImageIO.read(minio.getObject(request.getParameter(minio.getConfig().getPreviewImageParameterName())));
                    response.setContentType("image/png");
                    os = response.getOutputStream();
                    if (image != null) {
                        ImageIO.write(image, "png", os);
                    }
                } catch (IOException e) {
                    //
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }
            }
        }

    }

}
