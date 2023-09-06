package cn.hiboot.mcn.cloud.client;

import cn.hiboot.mcn.cloud.security.resource.LoginRsp;
import cn.hiboot.mcn.cloud.security.resource.TokenResolver;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * RestClientAutoConfiguration
 *
 * @author DingHao
 * @since 2023/1/3 14:58
 */
@AutoConfiguration(after = RestTemplateAutoConfiguration.class)
@EnableConfigurationProperties(RestClientProperties.class)
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(RestTemplateBuilder.class)
@ConditionalOnProperty(prefix = "rest.client",name = "enable",havingValue = "true",matchIfMissing = true)
public class RestClientAutoConfiguration {

    private final RestClientProperties properties;
    private final ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers;

    public RestClientAutoConfiguration(RestClientProperties properties, ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers) {
        this.properties = properties;
        this.restTemplateBuilderCustomizers = restTemplateBuilderCustomizers;
    }

    @Bean
    @ConditionalOnMissingBean(name = "restTemplate")
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return restTemplate0(builder);
    }

    private RestTemplate restTemplate0(RestTemplateBuilder builder) {
        builder.setReadTimeout(properties.getReadTimeout());
        builder.setConnectTimeout(properties.getConnectTimeout());
        restTemplateBuilderCustomizers.orderedStream().forEachOrdered(b -> b.custom(builder));
        return builder.build();
    }

    @Bean
    @LoadBalanced
    @ConditionalOnClass(name = "org.springframework.cloud.client.loadbalancer.LoadBalanced")
    @ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
    RestTemplate loadBalancedRestTemplate(RestTemplateBuilder builder) {
        return restTemplate0(builder);
    }

    @Bean
    @ConditionalOnMissingBean
    TokenResolver tokenResolver(RestTemplate restTemplate,RestTemplate loadBalancedRestTemplate, @Value("${token.service}") String tokenService){
        RestClient restClient = new RestClient(tokenService.split("\\.").length == 4 ? restTemplate : loadBalancedRestTemplate);
        return apk -> {
            LoginRsp rs = restClient.get("http://"+tokenService+"/sso/login/{apk}", LoginRsp.class, McnUtils.put("apk",apk));
            return new RestResp<>(rs);
        };
    }

}
