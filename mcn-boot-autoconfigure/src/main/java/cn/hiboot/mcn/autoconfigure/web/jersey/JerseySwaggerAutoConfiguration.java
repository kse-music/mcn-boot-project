package cn.hiboot.mcn.autoconfigure.web.jersey;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import cn.hiboot.mcn.core.util.McnUtils;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.util.ReflectionUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRegistration;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SpringComponentProvider.class, ServletRegistration.class })
@ConditionalOnMissingBean(
        type = {"org.glassfish.jersey.server.ResourceConfig"}
)
@EnableConfigurationProperties({JerseySwaggerProperties.class})
@AutoConfigureBefore(JerseyAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JerseySwaggerAutoConfiguration extends ResourceConfig {

    private final JerseySwaggerProperties jersey;

    public JerseySwaggerAutoConfiguration(JerseySwaggerProperties jersey, Environment environment) {
        jersey.setBasePackage(environment.getProperty((McnPropertiesPostProcessor.APP_BASE_PACKAGE)));
        this.jersey = jersey;
    }

    @Bean
    public ResourceConfigCustomizer resourceRegister() {
        return config -> {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Path.class));
            scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
            String otherResourcePackage = jersey.getOtherResourcePackage();
            Set<String> packages = new HashSet<>(Arrays.asList(jersey.getBasePackage()));
            if (StringUtils.hasLength(otherResourcePackage)) {
                packages.addAll(Arrays.asList(StringUtils.tokenizeToStringArray(otherResourcePackage, ",")));
            }
            Set<Class<?>> allClasses = new HashSet<>();//maybe need register JacksonJsonProvider.class
            for (String pkg : packages) {
                Set<Class<?>> collect = scanner.findCandidateComponents(pkg).stream()
                        .map(beanDefinition -> ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), this.getClassLoader()))
                        .collect(Collectors.toSet());
                allClasses.addAll(collect);
            }


            //添加全局异常处理器
            allClasses.add(GlobalExceptionHandler.class);
            //添加自定义异常处理器
            String classes = jersey.getSingleResource();
            if (StringUtils.hasLength(classes)) {
                allClasses.add(getExceptionHandlerClass(classes));
            }

            String path = McnUtils.dealUrlPath(jersey.getBasePath());

            config.registerClasses(allClasses)
                    .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true)
                    .property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, path+"/Swagger/(fonts|images|css|js|lang|lib)/.*")
                    .property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);

            //register MultiPartFeature
            if(ClassUtils.isPresent("org.glassfish.jersey.media.multipart.MultiPartFeature",null)){
                config.registerClasses(MultiPartFeature.class);
            }

            //init swagger
            initSwagger(config);

        };
    }

    private Class<?> getExceptionHandlerClass(String className) throws LinkageError {
        try {
            Class<?> exceptionClass = ClassUtils.forName(className, null);
            Assert.isAssignable(ExceptionMapper.class, exceptionClass);
            return exceptionClass;
        } catch (ClassNotFoundException ex) {
            throw new ApplicationContextException("Failed to load exception handler class [" + className + "]", ex);
        }
    }

    private void initSwagger(ResourceConfig config){
        if(jersey.getInit() && ClassUtils.isPresent("io.swagger.jaxrs.listing.ApiListingResource",null)){
            config.registerClasses(ApiListingResource.class, SwaggerSerializers.class);
            BeanConfig beanConfig = new BeanConfig(){
                @Override
                public Set<Class<?>> classes() {
                    Set<Class<?>> classes = super.classes();
                    if(jersey.getAuthHeader()){
                        for (Class<?> cls : classes) {
                            javax.ws.rs.Path apiPath = ReflectionUtils.getAnnotation(cls, javax.ws.rs.Path.class);
                            if(apiPath != null){
                                boolean clsHasIgnoreAuth = ReflectionUtils.getAnnotation(cls, IgnoreAuth.class) != null;
                                String clsPath = apiPath.value();
                                if(!clsPath.startsWith("/")){
                                    clsPath = "/"+clsPath;
                                }
                                if(!clsPath.endsWith("/")){
                                    clsPath += "/";
                                }
                                Method methods[] = cls.getMethods();
                                for (Method method : methods) {
                                    javax.ws.rs.Path methodPath = ReflectionUtils.getAnnotation(method, javax.ws.rs.Path.class);
                                    if(methodPath != null){
                                        String mp = (clsPath+methodPath.value()).replace("//","/");
                                        if(mp.endsWith("/")){
                                            mp = mp.substring(0,mp.lastIndexOf("/"));
                                        }
                                        if(clsHasIgnoreAuth){
                                            SwaggerReaderListener.ignoreMethod.add(mp,extractMethod(method));
                                            continue;
                                        }
                                        boolean methodHasIgnoreAuth = ReflectionUtils.getAnnotation(method, IgnoreAuth.class) != null;
                                        if(methodHasIgnoreAuth){
                                            SwaggerReaderListener.ignoreMethod.add(mp,extractMethod(method));
                                        }
                                    }
                                }
                            }
                        }
                        classes.add(SwaggerReaderListener.class);
                    }
                    return classes;
                }
            };
            beanConfig.setVersion(jersey.getVersion());
            beanConfig.setTitle(jersey.getTitle());
            if(Objects.nonNull(jersey.getHost())){
                beanConfig.setHost(jersey.getHost());
            }else{
                beanConfig.setHost(jersey.getIp() + ":" + jersey.getPort());
            }
            beanConfig.setBasePath(jersey.getBasePath());
            beanConfig.setResourcePackage(jersey.getResourcePackage());
            beanConfig.setScan();
            if(ClassUtils.isPresent("org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature",null)){
                config.property(FreemarkerMvcFeature.TEMPLATE_BASE_PATH, "META-INF/resources/Swagger")
                        .property(FreemarkerMvcFeature.CACHE_TEMPLATES, new Boolean(false))
                        .registerClasses(FreemarkerMvcFeature.class,SwaggerView.class);
            }
        }
    }

    private String extractMethod(Method method){
        if (method.getAnnotation(javax.ws.rs.GET.class) != null) {
            return "GET";
        } else if (method.getAnnotation(javax.ws.rs.PUT.class) != null) {
            return "PUT";
        } else if (method.getAnnotation(javax.ws.rs.POST.class) != null) {
            return "POST";
        } else if (method.getAnnotation(javax.ws.rs.DELETE.class) != null) {
            return "DELETE";
        } else if (method.getAnnotation(javax.ws.rs.OPTIONS.class) != null) {
            return "OPTIONS";
        } else if (method.getAnnotation(javax.ws.rs.HEAD.class) != null) {
            return "HEAD";
        } else if (method.getAnnotation(PATCH.class) != null) {
            return "PATCH";
        }
        return "";
    }

}
