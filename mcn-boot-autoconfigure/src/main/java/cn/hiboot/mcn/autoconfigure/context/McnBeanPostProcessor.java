package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * McnBeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor {

    private static final boolean mongoPropertiesPresent;
    private static final boolean dataRedisPropertiesPresent;

    static {
        mongoPropertiesPresent = ClassUtils.isPresent("org.springframework.boot.mongodb.autoconfigure.MongoProperties", null);
        dataRedisPropertiesPresent = ClassUtils.isPresent("org.springframework.boot.data.redis.autoconfigure.DataRedisProperties", null);
    }

    private final ApplicationContext context;

    public McnBeanPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ObjectMapper objectMapper) {
            return setObjectMapper(objectMapper);
        }
        postProcessAfterInitialization(bean);
        return bean;
    }

    private void postProcessAfterInitialization(Object bean) {
        if (mongoPropertiesPresent && bean instanceof MongoProperties mongoProperties) {
            mappingMongoConfig(mongoProperties);
        } else if (dataRedisPropertiesPresent && bean instanceof DataRedisProperties redisProperties) {
            mappingRedisConfig(redisProperties);
        }
    }

    private ObjectMapper setObjectMapper(ObjectMapper objectMapper) {
        Environment environment = context.getEnvironment();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.setResourceLoader(context);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JsonTypeName.class));
        Set<String> basePackages = new HashSet<>(4);
        Collections.addAll(basePackages, environment.getProperty("jackson.subtypes.package", environment.getProperty(ConfigProperties.APP_BASE_PACKAGE, "")).split(","));
        ObjectMapper configuredObjectMapper = objectMapper.rebuild()
                .registerSubtypes(basePackages.stream().filter(StringUtils::hasText).flatMap(basePackage -> scanner.findCandidateComponents(basePackage).stream()).map(candidate -> ClassUtils.resolveClassName(candidate.getBeanClassName(), context.getClassLoader())).collect(Collectors.toList()))
                .build();
        JacksonUtils.setObjectMapper(configuredObjectMapper);
        return configuredObjectMapper;
    }

    private void mappingMongoConfig(MongoProperties mongoProperties) {
        if (mongoProperties.getUri() != null) {
            return;
        }
        Environment environment = context.getEnvironment();
        String mongoAddress = environment.getProperty("mongo.addrs");
        if (StringUtils.hasText(mongoAddress)) {
            String username = environment.getProperty("mongo.username");
            String password = environment.getProperty("mongo.password");
            String uri = mongoAddress;
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                uri = replace(username) + ":" + replace(password) + "@" + mongoAddress;
            }
            mongoProperties.setUri("mongodb://" + uri);
        }
    }

    private String replace(String str) {
        return str.replace(":", "%3A").replace("@", "%40").replace("/", "%2F");
    }

    private void mappingRedisConfig(DataRedisProperties redisProperties) {
        Environment environment = context.getEnvironment();
        String redisAddress = environment.getProperty("redis.addrs");
        if (StringUtils.hasText(redisAddress)) {
            String master = environment.getProperty("redis.sentinel");
            List<String> hosts = Arrays.asList(StringUtils.commaDelimitedListToStringArray(redisAddress));
            if (StringUtils.hasText(master)) {//sentinel
                DataRedisProperties.Sentinel sentinel = redisProperties.getSentinel();
                if (sentinel == null) {
                    sentinel = new DataRedisProperties.Sentinel();
                }
                sentinel.setMaster(master);
                sentinel.setNodes(hosts);
                if (StringUtils.hasText(redisProperties.getPassword()) && ObjectUtils.isEmpty(sentinel)) {
                    sentinel.setPassword(redisProperties.getPassword());//normal use data pwd as sentinel pwd
                }
                redisProperties.setSentinel(sentinel);
            } else {
                if (hosts.size() == 1) {//standalone
                    String[] hp = hosts.get(0).split(":");
                    redisProperties.setHost(hp[0]);
                    redisProperties.setPort(Integer.parseInt(hp[1]));
                } else { //cluster
                    DataRedisProperties.Cluster cluster = redisProperties.getCluster();
                    if (cluster == null) {
                        cluster = new DataRedisProperties.Cluster();
                    }
                    cluster.setNodes(hosts);
                    redisProperties.setCluster(cluster);
                }
            }
        }
    }

}
