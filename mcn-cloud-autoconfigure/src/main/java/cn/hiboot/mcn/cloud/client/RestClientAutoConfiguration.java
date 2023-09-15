package cn.hiboot.mcn.cloud.client;

import cn.hiboot.mcn.cloud.security.resource.ApkResolver;
import cn.hiboot.mcn.cloud.security.resource.LoginRsp;
import cn.hiboot.mcn.cloud.security.resource.TokenResolver;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * RestClientAutoConfiguration
 *
 * @author DingHao
 * @since 2023/1/3 14:58
 */
@AutoConfiguration(after = {RestTemplateAutoConfiguration.class, WebClientAutoConfiguration.class})
@EnableConfigurationProperties(RestClientProperties.class)
public class RestClientAutoConfiguration {

    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnBean(RestTemplateBuilder.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletClientAutoConfiguration {

        private final RestClientProperties properties;
        private final ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers;

        public ServletClientAutoConfiguration(RestClientProperties properties, ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers) {
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
            RestTemplate restClient = isIp(tokenService) ? restTemplate : loadBalancedRestTemplate;
            return apk -> restClient.exchange("http://"+tokenService+"/sso/login/{apk}", HttpMethod.GET, null,loginRspType()).getBody();
        }

    }

    private static boolean isIp(String host){
        return host.split("\\.").length == 4;
    }

    private static ParameterizedTypeReference<RestResp<LoginRsp>> loginRspType(){
        return ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(RestResp.class,LoginRsp.class).getType());
    }

    @ConditionalOnClass(WebClient.class)
    @ConditionalOnBean(WebClient.Builder.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveClientAutoConfiguration {

        private final RestClientProperties properties;

        public ReactiveClientAutoConfiguration(RestClientProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean(name = "webClient")
        WebClient webClient(WebClient.Builder builder) {
            return webClient0(builder);
        }

        private WebClient webClient0(WebClient.Builder builder) {
            HttpClient httpClient = HttpClient.create().responseTimeout(properties.getReadTimeout());
            ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
            return builder.clientConnector(connector).build();
        }

        @Bean
        @LoadBalanced
        @ConditionalOnClass(name = "org.springframework.cloud.client.loadbalancer.LoadBalanced")
        @ConditionalOnMissingBean(name = "loadBalancedWebClient")
        WebClient loadBalancedWebClient(WebClient.Builder builder) {
            return webClient0(builder);
        }

        @Bean
        @ConditionalOnMissingBean
        ApkResolver apkResolver(WebClient webClient, WebClient loadBalancedWebClient, @Value("${token.service}") String tokenService){
            WebClient restClient = isIp(tokenService) ? webClient : loadBalancedWebClient;
            return apk -> Mono.fromCallable(() -> apk).flatMap(a -> {
               String uri = UriComponentsBuilder.fromUriString("http://"+tokenService+"/sso/login/{apk}").buildAndExpand(apk).toUriString();
               return restClient.get().uri(uri).retrieve().bodyToMono(loginRspType());
           });
        }
    }

}
