package cn.hiboot.mcn.autoconfigure.json;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JsonAutoConfiguration
 *
 * @author DingHao
 * @since 2025/11/26 15:30
 */
@AutoConfiguration
@ConditionalOnClass({JacksonAutoConfiguration.class})
public class JsonAutoConfiguration {

    @Bean
    static JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer(ApplicationContext context) {
        return builder -> {
            Environment environment = context.getEnvironment();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment);
            scanner.setResourceLoader(context);
            scanner.addIncludeFilter(new AnnotationTypeFilter(JsonTypeName.class));
            Set<String> basePackages = new HashSet<>(4);
            Collections.addAll(basePackages, environment.getProperty("jackson.subtypes.package", environment.getProperty(ConfigProperties.APP_BASE_PACKAGE, "")).split(","));
            builder.registerSubtypes(basePackages.stream().filter(StringUtils::hasText).flatMap(basePackage -> scanner.findCandidateComponents(basePackage).stream()).map(candidate -> ClassUtils.resolveClassName(candidate.getBeanClassName(), context.getClassLoader())).collect(Collectors.toList()));
        };
    }

}
