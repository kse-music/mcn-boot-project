package cn.hiboot.mcn.cloud.gateway;

import cn.hiboot.mcn.autoconfigure.web.exception.AbstractExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.reactor.GlobalServerExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.swagger.IgnoreApi;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * GatewayExtensionAutoConfiguration
 *
 * @author DingHao
 * @since 2022/5/25 1:34
 */
@AutoConfiguration
@ConditionalOnClass(GatewayAutoConfiguration.class)
public class GatewayExtensionAutoConfiguration {

    @RestController
    @ConditionalOnProperty(prefix = "gateway.fallback",name = "enabled",havingValue = "true",matchIfMissing = true)
    protected static class DefaultFallbackRestController{
        AbstractExceptionHandler exceptionHandler;

        public DefaultFallbackRestController(GlobalExceptionProperties properties) {
            exceptionHandler = new GlobalServerExceptionHandler(properties);
        }

        @IgnoreApi
        @RequestMapping("fallback")
        public Mono<RestResp<?>> fallback(ServerWebExchange exchange) {
            return Mono.fromSupplier(() -> {
                Throwable ex = exchange.getAttribute(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
                if(ex == null){
                    return RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR);
                }
                RestResp<Throwable> resp = exceptionHandler.handleException(ex);
                Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                resp.setErrorInfo((route == null ? "" : route.getUri().getHost()) + " service " +resp.getErrorInfo());
                return resp;
//                return RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR,(route == null ? "" : route.getUri().getHost())+"服务不可用或请求超时");
            });
        }

    }

}
