package cn.hiboot.mcn.cloud.security.token;


import cn.hiboot.mcn.cloud.client.RestClientAutoConfiguration;
import cn.hiboot.mcn.cloud.security.resource.LoginRsp;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * TokenResolverAutoConfiguration
 *
 * @author DingHao
 * @since 2024/10/14 11:06
 */
@AutoConfiguration(after = RestClientAutoConfiguration.class)
@EnableConfigurationProperties(TokenResolverProperties.class)
public class TokenResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    static TokenResolverCache defaultTokenResolverCache(TokenResolverProperties properties) {
        return new DefaultTokenResolverCache(properties.getAccessValidity());
    }

    @ConditionalOnBean(RestTemplate.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTokenResolverConfiguration {

        @Bean
        @ConditionalOnMissingBean
        TokenResolver tokenResolver(TokenResolverCache tokenResolverCache,
                                    RestTemplate restTemplate,
                                    RestTemplate loadBalancedRestTemplate,
                                    @Value("${token.service}") String tokenService) {
            RestTemplate restClient = isIp(tokenService) ? restTemplate : loadBalancedRestTemplate;
            String url = tokenUrl(tokenService);
            return apk -> tokenResolverCache.get(apk, () -> {
                RestResp<LoginRsp> resp = restClient.exchange(restClient.getUriTemplateHandler().expand(url, McnUtils.put("apk", apk)), HttpMethod.GET, null, loginRspType()).getBody();
                if (resp == null || resp.isFailed()) {
                    return null;
                }
                return resp.getData();
            });
        }

    }

    private static boolean isIp(String host) {
        return host.split("\\.").length == 4;
    }

    private static ParameterizedTypeReference<RestResp<LoginRsp>> loginRspType() {
        return ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(RestResp.class, LoginRsp.class).getType());
    }

    private static String tokenUrl(String tokenService) {
        return "http://" + tokenService + "/sso/login/{apk}";
    }

    @ConditionalOnBean(WebClient.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveTokenResolverConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServerTokenResolver serverTokenResolver(TokenResolverCache tokenResolverCache,
                                                WebClient webClient,
                                                WebClient loadBalancedWebClient,
                                                @Value("${token.service}") String tokenService) {
            WebClient restClient = isIp(tokenService) ? webClient : loadBalancedWebClient;
            String url = tokenUrl(tokenService);
            return apk -> Mono.fromSupplier(() -> tokenResolverCache.get(apk))
                    .switchIfEmpty(Mono.defer(() -> {
                        String uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(apk).toUriString();
                        return restClient.get().uri(uri).retrieve().bodyToMono(loginRspType()).map(RestResp::getData).doOnNext(data -> {
                            if (data != null) {
                                tokenResolverCache.put(apk, data);
                            }
                        });
                    }));
        }

    }


}
