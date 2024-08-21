package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    static class JdbcFileUploadInfoCache implements FileUploadInfoCache {

        private static final String SELECT_SQL = "SELECT * FROM c_files WHERE md5 = ?";
        private static final String INSERT_SQL = "INSERT INTO c_files (upload_id, md5, filename, upload_urls, chunk_num) VALUES (?, ?, ?, ?, ?)";
        private static final String UPDATE_SQL = "UPDATE c_files SET upload_urls = ? WHERE md5 = ?";
        private static final String DELETE_SQL = "DELETE FROM c_files WHERE md5 = ?";
        private final JdbcTemplate jdbcTemplate;
        private final MinioProperties config;

        public JdbcFileUploadInfoCache(JdbcTemplate jdbcTemplate, MinioProperties config) {
            this.jdbcTemplate = jdbcTemplate;
            this.config = config;
        }

        @Override
        public FileUploadInfo get(String filename) {
            return get("SELECT * FROM c_files WHERE filename = ?", filename);
        }

        @Override
        public FileUploadInfo get(FileUploadInfo fileUploadInfo) {
            FileUploadInfo f = get(SELECT_SQL, fileUploadInfo.getMd5());
            if (f != null) {
                long diffInMillis = McnUtils.now().getTime() - f.getCreateAt().getTime();
                long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
                if (McnUtils.isNotNullAndEmpty(f.getUploadUrls()) && diffInSeconds > config.getExpire()) {
                    remove(f);
                    return null;
                }
            }
            return f;
        }

        private FileUploadInfo get(String sql, String value) {
            List<FileUploadInfo> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
                FileUploadInfo f = new FileUploadInfo();
                f.setChunkNum(rs.getInt("chunk_num"));
                f.setMd5(rs.getString("md5"));
                f.setUploadId(rs.getString("upload_id"));
                f.setFilename(rs.getString("filename"));
                f.setCreateAt(rs.getTimestamp("create_at"));
                String urls = rs.getString("upload_urls");
                if (urls != null) {
                    f.setUploadUrls(Arrays.asList(urls.split(",")));
                }
                return f;
            }, value);
            if (result.isEmpty()) {
                return null;
            }
            return result.get(0);
        }

        @Override
        public void put(FileUploadInfo fileUploadInfo) {
            FileUploadInfo uploadInfo = get(fileUploadInfo);
            String uploadUrls = null;
            if (uploadInfo == null) {
                uploadUrls = String.join(",", fileUploadInfo.getUploadUrls());
                jdbcTemplate.update(INSERT_SQL, fileUploadInfo.getUploadId(), fileUploadInfo.getMd5(), fileUploadInfo.getFilename(), uploadUrls, fileUploadInfo.getChunkNum());
            } else {
                jdbcTemplate.update(UPDATE_SQL, uploadUrls, fileUploadInfo.getMd5());
            }
        }

        @Override
        public void remove(FileUploadInfo fileUploadInfo) {
            jdbcTemplate.update(DELETE_SQL, fileUploadInfo.getMd5());
        }

    }

}
