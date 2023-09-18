package cn.hiboot.mcn.autoconfigure.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.util.ClassUtils;

/**
 * SwaggerRunFailureAnalyzer
 *
 * @author DingHao
 * @since 2023/9/18 17:15
 */
class SwaggerRunFailureAnalyzer extends AbstractFailureAnalyzer<IllegalStateException> {
    private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, IllegalStateException cause) {
        if(ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && cause.getMessage().equals("Error processing condition on cn.hiboot.mcn.autoconfigure.web.swagger.SwaggerAutoConfiguration.openApi")){
            return new FailureAnalysis("need springdoc-openapi-starter-webflux-ui dependency", getAction(), cause);
        }
        return null;
    }

    private String getAction(){
        return "exclude springdoc-openapi-starter-webmvc-ui in mvc-swagger2 and add springdoc-openapi-starter-webflux-ui in pom.xml";
    }
}
