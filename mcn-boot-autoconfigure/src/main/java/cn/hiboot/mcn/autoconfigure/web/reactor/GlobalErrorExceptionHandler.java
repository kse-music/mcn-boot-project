package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *
 * GlobalErrorExceptionHandler
 *
 * @author DingHao
 * @since 2022/5/23 23:08
 */
@Order(-1)
@ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
public class GlobalErrorExceptionHandler extends DefaultErrorWebExceptionHandler implements EnvironmentAware {

    @Autowired
    private WebFluxProperties webFluxProperties;

    private boolean overrideHttpError;

    public GlobalErrorExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ServerProperties serverProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), serverProperties.getError(), applicationContext);
    }

    @Autowired
    public void setServerCodecConfigurer(ServerCodecConfigurer serverCodecConfigurer) {
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody, Map<String, Object> error) {
        return responseBody.bodyValue(ConfigProperties.errorView(error, webFluxProperties.getBasePath()));
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        if(overrideHttpError){
            Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
            int statusCode = (int) error.get("status");
            return ServerResponse.status(HttpStatus.OK.value()).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(RestResp.error(ExceptionKeys.mappingCode(statusCode))));
        }
        return super.renderErrorResponse(request);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.overrideHttpError = environment.getProperty("http.error.override",Boolean.class,true);
    }

}
