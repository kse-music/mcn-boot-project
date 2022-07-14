package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionHelper;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 *
 * GlobalErrorExceptionHandler
 *
 * @author DingHao
 * @since 2022/5/23 23:08
 */
@ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
public class GlobalErrorExceptionHandler extends DefaultErrorWebExceptionHandler implements EnvironmentAware, Ordered {

    private final Logger log = LoggerFactory.getLogger(GlobalErrorExceptionHandler.class);

    @Autowired
    private WebFluxProperties webFluxProperties;

    @Autowired
    private GlobalExceptionProperties properties;

    private Environment environment;
    private ExceptionHelper exceptionHelper;

    public GlobalErrorExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ServerProperties serverProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), serverProperties.getError(), applicationContext);
    }

    @Autowired
    public void setServerCodecConfigurer(ServerCodecConfigurer serverCodecConfigurer) {
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @PostConstruct
    private void init(){
        exceptionHelper = new ExceptionHelper(properties,environment,log);
    }

    @Override
    protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody, Map<String, Object> error) {
        return responseBody.bodyValue(ConfigProperties.errorView(error, webFluxProperties.getBasePath()));
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable ex = getError(request);
        if (ex instanceof ResponseStatusException) {
            if(exceptionHelper.isOverrideHttpError()){
                Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
                int statusCode = (int) error.get("status");
                return ServerResponse.status(HttpStatus.OK.value()).contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(RestResp.error(ExceptionKeys.mappingCode(statusCode))));
            }
        }else {
            exceptionHelper.logError(ex);
            return ServerResponse.status(HttpStatus.OK.value()).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(exceptionHelper.doHandleException(ex)));
        }
        return super.renderErrorResponse(request);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
