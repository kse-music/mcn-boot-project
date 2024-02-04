package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.DefaultExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *
 * GlobalErrorExceptionHandler
 *
 * @author DingHao
 * @since 2022/5/23 23:08
 */
@ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
public class GlobalErrorExceptionHandler extends DefaultErrorWebExceptionHandler implements HttpStatusCodeResolver,EnvironmentAware, Ordered {
    @Value("${http.error.override:true}")
    private boolean overrideHttpError;
    private WebFluxProperties webFluxProperties;
    private int order;
    private ExceptionHandler exceptionHandler;

    public GlobalErrorExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ServerProperties serverProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), serverProperties.getError(), applicationContext);
    }

    @Autowired
    public void configGlobalErrorExceptionHandler(ServerCodecConfigurer serverCodecConfigurer, WebFluxProperties webFluxProperties, DefaultExceptionHandler exceptionHandler) {
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
        this.webFluxProperties = webFluxProperties;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody, Map<String, Object> error) {
        return responseBody.bodyValue(ConfigProperties.errorView(error, webFluxProperties.getBasePath()));
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        return ServerResponse.status(HttpStatus.OK.value()).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(exceptionHandler.handleException(getError(request))));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.order = environment.getProperty("mcn.exception.handler.reactor.order",Integer.class, -1);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Integer resolve(Throwable ex) {
        if(ex instanceof ResponseStatusException responseStatusException){
            if(overrideHttpError){
                return mappingCode(responseStatusException.getStatusCode());
            }
            throw ServiceException.newInstance(ex);
        }
        return null;
    }
}
