package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Minio文件上传
 *
 * @author DingHao
 * @since 2021/6/28 22:02
 */
@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
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
    @ConditionalOnMissingBean
    public DefaultMinioClient minioClient(){
        MinioAsyncClient.Builder builder = MinioAsyncClient.builder();
        customizers.orderedStream().forEach(customizer->customizer.customize(builder));
        return new DefaultMinioClient(config,builder);
    }

    @Bean
    @ConditionalOnMissingBean
    public Minio minio(DefaultMinioClient minioClient, FileUploadInfoCache fileUploadInfoCache){
        return new DefaultMinio(minioClient, fileUploadInfoCache);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass("org.springframework.jdbc.core.JdbcTemplate")
    @ConditionalOnMissingBean(FileUploadInfoCache.class)
    protected static class DefaultFileUploadInfoCacheConfig {

        @Bean
        public FileUploadInfoCache defaultFileUploadInfoCache(MinioProperties config){
            return new DefaultFileUploadInfoCache(config);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(FileUploadInfoCache.class)
    static class JdbcFileUploadInfoCacheConfig  {

        @Bean
        public FileUploadInfoCache jdbcFileUploadInfoCache(JdbcTemplate jdbcTemplate, MinioProperties config){
            return new JdbcFileUploadInfoCache(jdbcTemplate, config);
        }

    }

}
