package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * McnBeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext context;
    private final boolean mongoPropertiesExist;
    private final boolean redisPropertiesExist;

    public McnBeanPostProcessor(ApplicationContext context) {
        this.context = context;
        this.mongoPropertiesExist = ClassUtils.isPresent("org.springframework.boot.mongodb.autoconfigure.MongoProperties", this.getClass().getClassLoader());
        this.redisPropertiesExist = ClassUtils.isPresent("org.springframework.boot.data.redis.autoconfigure.DataRedisProperties", this.getClass().getClassLoader());
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        postProcessAfterInitialization(bean);
        return bean;
    }

    private void postProcessAfterInitialization(Object bean) {
        if (bean instanceof ObjectMapper objectMapper) {
            JacksonUtils.setObjectMapper(objectMapper);
        } else if (mongoPropertiesExist && bean instanceof MongoProperties mongoProperties) {
            mappingMongoConfig(mongoProperties);
        } else if (redisPropertiesExist && bean instanceof DataRedisProperties redisProperties) {
            mappingRedisConfig(redisProperties);
        }
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