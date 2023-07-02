package cn.hiboot.mcn.autoconfigure.web.groovy;

import cn.hiboot.mcn.autoconfigure.web.swagger.IgnoreApi;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GroovyWebAutoConfiguration
 *
 * @author DingHao
 * @since 2021/12/22 15:31
 */
@AutoConfiguration
@ConditionalOnClass(GroovyObject.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties(GroovyWebProperties.class)
@ConditionalOnProperty(prefix = "groovy.web", value = "enable", havingValue = "true")
public class GroovyWebAutoConfiguration {
    
    private final GroovyWebProperties groovyDebugProperties;
    private final ObjectProvider<GroovyWebCustomizer> customizers;

    public GroovyWebAutoConfiguration(GroovyWebProperties groovyDebugProperties, ObjectProvider<GroovyWebCustomizer> customizers) {
        this.groovyDebugProperties = groovyDebugProperties;
        this.customizers = customizers;
    }

    @Bean
    @ConditionalOnMissingBean
    public GroovyShell groovyShell(Binding groovyBinding) {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setSourceEncoding(groovyDebugProperties.getSourceEncoding());
        compilerConfiguration.setDebug(groovyDebugProperties.isDebug());
        compilerConfiguration.setTargetDirectory(groovyDebugProperties.getTargetDirectory());
        compilerConfiguration.setScriptBaseClass(groovyDebugProperties.getScriptBaseClass());
        if(groovyDebugProperties.getClasspath() != null){
            compilerConfiguration.setClasspath(groovyDebugProperties.getClasspath());
        }
        for (GroovyWebCustomizer customizer : customizers) {
            customizer.customize(groovyBinding,compilerConfiguration);
        }
        return new GroovyShell(groovyClassLoader, groovyBinding, compilerConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public Binding groovyBinding(ApplicationContext applicationContext) {
        Binding groovyBinding = new Binding();
        Map<String, Object> beanMap = applicationContext.getBeansOfType(Object.class);
        for (String beanName : beanMap.keySet()) {
            groovyBinding.setVariable(beanName, beanMap.get(beanName));
        }
        return groovyBinding;
    }

    /**
     * @ignore
     */
    @RestController
    @IgnoreApi
    static class GroovyWebController {

        GroovyShell groovyShell;

        GroovyWebController(GroovyShell groovyShell) {
            this.groovyShell = groovyShell;
        }

        @PostMapping("_groovyDebug_")
        public Object groovyDebug(@RequestBody String scriptContent) {
            return groovyShell.parse(scriptContent).run();
        }

    }

}
