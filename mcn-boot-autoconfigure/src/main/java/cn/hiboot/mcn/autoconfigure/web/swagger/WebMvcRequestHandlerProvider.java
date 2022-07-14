package cn.hiboot.mcn.autoconfigure.web.swagger;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.RequestHandler;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static springfox.documentation.builders.BuilderDefaults.nullToEmptyList;
import static springfox.documentation.spi.service.contexts.Orderings.byPatternsCondition;
import static springfox.documentation.spring.web.paths.Paths.ROOT;

/**
 * WebMvcRequestHandlerProvider
 *
 * @author DingHao
 * @since 2022/7/14 16:15
 */
public class WebMvcRequestHandlerProvider implements RequestHandlerProvider {
    private final List<RequestMappingInfoHandlerMapping> handlerMappings;
    private final HandlerMethodResolver methodResolver;
    private final String contextPath;

    public WebMvcRequestHandlerProvider(Optional<ServletContext> servletContext,HandlerMethodResolver methodResolver,List<RequestMappingInfoHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
        this.methodResolver = methodResolver;
        this.contextPath = servletContext.map(ServletContext::getContextPath).orElse(ROOT);
    }

    @Override
    public List<RequestHandler> requestHandlers() {
        return nullToEmptyList(handlerMappings).stream()
                .filter(requestMappingInfoHandlerMapping ->
                        !("org.springframework.integration.http.inbound.IntegrationRequestMappingHandlerMapping"
                                .equals(requestMappingInfoHandlerMapping.getClass()
                                        .getName())))
                .map(toMappingEntries())
                .flatMap((entries -> StreamSupport.stream(entries.spliterator(), false)))
                .map(toRequestHandler())
                .sorted(byPatternsCondition())
                .collect(toList());
    }

    private Function<RequestMappingInfoHandlerMapping,
            Iterable<Map.Entry<RequestMappingInfo, HandlerMethod>>> toMappingEntries() {
        return input -> input.getHandlerMethods()
                .entrySet();
    }

    private Function<Map.Entry<RequestMappingInfo, HandlerMethod>, RequestHandler> toRequestHandler() {
        return input -> new WebMvcRequestHandler(
                contextPath,
                methodResolver,
                input.getKey(),
                input.getValue());
    }
}

