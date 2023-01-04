package cn.hiboot.mcn.cloud.client;

import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * RestTemplateBuilderCustomizer
 *
 * @author DingHao
 * @since 2023/1/3 15:03
 */
public interface RestTemplateBuilderCustomizer {
    void custom(RestTemplateBuilder builder);
}
