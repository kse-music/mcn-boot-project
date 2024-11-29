package cn.hiboot.mcn.cloud.client;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * RestClientAutoConfiguration
 *
 * @author DingHao
 * @since 2023/1/3 14:58
 */
@AutoConfiguration(after = {WebClientAutoConfiguration.class, RestTemplateAutoConfiguration.class}, afterName = "org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration")
@EnableConfigurationProperties(RestClientProperties.class)
public class RestClientAutoConfiguration {

    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnBean(RestTemplateBuilder.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletClientConfiguration {

        private final RestClientProperties properties;
        private final ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers;

        public ServletClientConfiguration(RestClientProperties properties, ObjectProvider<RestTemplateBuilderCustomizer> restTemplateBuilderCustomizers) {
            this.properties = properties;
            this.restTemplateBuilderCustomizers = restTemplateBuilderCustomizers;
        }

        @Bean
        @ConditionalOnMissingBean(name = "restTemplate")
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            return restTemplate0(builder);
        }

        private RestTemplate restTemplate0(RestTemplateBuilder builder) {
            builder.readTimeout(properties.getReadTimeout());
            builder.connectTimeout(properties.getConnectTimeout());
            restTemplateBuilderCustomizers.orderedStream().forEach(b -> b.custom(builder));
            return builder.build();
        }

        @Bean
        @LoadBalanced
        @ConditionalOnClass(LoadBalanced.class)
        @ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
        RestTemplate loadBalancedRestTemplate(RestTemplateBuilder builder) {
            return restTemplate0(builder);
        }

    }

    @ConditionalOnClass(WebClient.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Import(ReactiveClientConfiguration.LoadBalancedClientConfiguration.class)
    static class ReactiveClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "webClient")
        WebClient webClient(WebClient.Builder webClientBuilder, RestClientProperties properties) {
            return webClient0(webClientBuilder, properties);
        }

        private static WebClient webClient0(WebClient.Builder builder, RestClientProperties properties) {
            HttpClient httpClient = HttpClient.create().responseTimeout(properties.getReadTimeout())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis());
            ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
            return builder.clientConnector(connector).build();
        }

        @ConditionalOnBean(DeferringLoadBalancerExchangeFilterFunction.class)
        static class LoadBalancedClientConfiguration {

            @Bean
            @ConditionalOnMissingBean(name = "loadBalancedWebClient")
            WebClient loadBalancedWebClient(WebClient.Builder webClientBuilder, RestClientProperties properties, DeferringLoadBalancerExchangeFilterFunction exchangeFilterFunction) {
                webClientBuilder.filter(exchangeFilterFunction);
                return webClient0(webClientBuilder, properties);
            }

        }

    }

}
