package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 *
 * McnBeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor{

    private final ConfigurableEnvironment environment;

    public McnBeanPostProcessor(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper){
            JacksonUtils.setObjectMapper((ObjectMapper) bean);
        }else {
            mappingDbCustomConfigToStandardConfig(bean);
        }
        return bean;
    }

    private void mappingDbCustomConfigToStandardConfig(Object bean){
        if(bean instanceof MongoProperties){
            mappingMongoConfig((MongoProperties) bean);
        }else if(bean instanceof RedisProperties){
            mappingRedisConfig((RedisProperties) bean);
        }
    }

    private void mappingMongoConfig(MongoProperties mongoProperties){
        if (mongoProperties.getUri() != null) {
            return;
        }
        String mongoAddress = environment.getProperty("mongo.addrs");
        if(StringUtils.hasText(mongoAddress)){
            String username = environment.getProperty("mongo.username");
            String password = environment.getProperty("mongo.password");
            String uri = mongoAddress;
            if(StringUtils.hasText(username) && StringUtils.hasText(password)){
                uri = replace(username)+":" + replace(password) + "@" + mongoAddress;
            }
            mongoProperties.setUri("mongodb://"+uri);
        }
    }

    private String replace(String str){
        return str.replace(":","%3A").replace("@","%40").replace("/","%2F");
    }

    private void mappingRedisConfig(RedisProperties redisProperties){
        String redisAddress = environment.getProperty("redis.addrs");
        if(StringUtils.hasText(redisAddress)){
            String master = environment.getProperty("redis.sentinel");
            List<String> hosts = Arrays.asList(StringUtils.commaDelimitedListToStringArray(redisAddress));
            if(StringUtils.hasText(master)){//sentinel
                RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
                if(sentinel == null){
                    sentinel = new RedisProperties.Sentinel();
                }
                sentinel.setMaster(master);
                sentinel.setNodes(hosts);
                if(StringUtils.hasText(redisProperties.getPassword()) && ObjectUtils.isEmpty(sentinel)){
                    sentinel.setPassword(redisProperties.getPassword());//normal use data pwd as sentinel pwd
                }
                redisProperties.setSentinel(sentinel);
            }else {
                if(hosts.size() == 1){//standalone
                    String[] hp = hosts.get(0).split(":");
                    redisProperties.setHost(hp[0]);
                    redisProperties.setPort(Integer.parseInt(hp[1]));
                }else { //cluster
                    RedisProperties.Cluster cluster = redisProperties.getCluster();
                    if(cluster == null){
                        cluster = new RedisProperties.Cluster();
                    }
                    cluster.setNodes(hosts);
                    redisProperties.setCluster(cluster);
                }
            }
        }
    }

}