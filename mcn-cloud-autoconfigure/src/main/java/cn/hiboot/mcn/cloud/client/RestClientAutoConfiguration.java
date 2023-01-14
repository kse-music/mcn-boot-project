package cn.hiboot.mcn.cloud.client;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestClientAutoConfiguration
 *
 * @author DingHao
 * @since 2023/1/3 14:58
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
@EnableConfigurationProperties(RestClientProperties.class)
@ConditionalOnClass(RestTemplate.class)
public class RestClientAutoConfiguration {

    private final RestClientProperties properties;
    private final ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers;

    public RestClientAutoConfiguration(RestClientProperties properties, ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers) {
        this.properties = properties;
        this.restTemplateBuilderCustomizers = restTemplateBuilderCustomizers;
    }

    @Bean
    @ConditionalOnMissingBean
    @LoadBalanced
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        builder.setReadTimeout(properties.getReadTimeout());
        builder.setConnectTimeout(properties.getConnectTimeout());
        restTemplateBuilderCustomizers.orderedStream().forEachOrdered(b -> b.custom(builder));
        return builder.build();
    }

}