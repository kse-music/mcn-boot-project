package cn.hiboot.mcn.cloud.gateway;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;

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

        @RequestMapping("fallback")
        public RestResp<?> fallback(ServerWebExchange exchange) {
            Throwable o = exchange.getAttribute(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
            if(o != null){
                return RestResp.error(o.getMessage());
            }
            return RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }

    }

}
