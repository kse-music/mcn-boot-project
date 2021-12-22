package cn.hiboot.mcn.autoconfigure.web.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * GroovyDebugAutoConfiguration
 *
 * @author DingHao
 * @since 2021/12/22 15:31
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(GroovyObject.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "groovy.debug", value = "enable", havingValue = "true")
public class GroovyDebugAutoConfiguration {

    private final ObjectProvider<BindingCustomizer> customizers;

    public GroovyDebugAutoConfiguration(ObjectProvider<BindingCustomizer> customizers) {
        this.customizers = customizers;
    }

    @Bean
    @ConditionalOnMissingBean
    public GroovyShell groovyShell(Binding groovyBinding) {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setSourceEncoding(StandardCharsets.UTF_8.displayName());
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
        for (BindingCustomizer customizer : customizers) {
            customizer.customize(groovyBinding);
        }
        return groovyBinding;
    }

    @RestController
    static class GroovyDebugController {

        GroovyShell groovyShell;

        GroovyDebugController(GroovyShell groovyShell) {
            this.groovyShell = groovyShell;
        }

        @PostMapping("_groovyDebug_")
        public Object groovyDebug(@RequestBody String scriptContent) {
            return groovyShell.parse(scriptContent).run();
        }

    }

}
