package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .build();
    }

    @Bean
    public Minio minio(MinioClient minioClient){
        return new Minio(minioClient);
    }

}
