package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.core.env.ConfigurableEnvironment;
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
        }else if(bean instanceof MongoProperties && StringUtils.hasText(environment.getProperty("mongo.addrs"))){//使用了自定义配置变量
            String mongoAddress = environment.getProperty("mongo.addrs");
            MongoProperties mongoProperties = (MongoProperties) bean;
            String username = mongoProperties.getUsername();
            String password = environment.getProperty("mongo.password");
            String uri = mongoAddress;
            if(StringUtils.hasText(username) && StringUtils.hasText(password)){
                uri = replace(username)+":" + replace(password) + "@" + mongoAddress;
            }
            mongoProperties.setUri("mongodb://"+uri);
        }else if(bean instanceof RedisProperties && StringUtils.hasText(environment.getProperty("redis.addrs"))){//使用了自定义配置变量
            RedisProperties redisProperties = (RedisProperties) bean;
            String master = environment.getProperty("redis.sentinel");
            List<String> hosts = Arrays.asList(StringUtils.commaDelimitedListToStringArray(environment.getProperty("redis.addrs")));
            if(StringUtils.hasText(master)){//哨兵
                RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
                sentinel.setMaster(master);
                sentinel.setNodes(hosts);
                redisProperties.setSentinel(sentinel);
            }else {
                if(hosts.size() == 1){//单机
                    String[] hp = hosts.get(0).split(":");
                    redisProperties.setHost(hp[0]);
                    redisProperties.setPort(Integer.parseInt(hp[1]));
                }else { //cluster
                    RedisProperties.Cluster cluster = new RedisProperties.Cluster();
                    cluster.setNodes(hosts);
                    redisProperties.setCluster(cluster);
                }
            }
        }
        return bean;
    }

    private String replace(String str){
        return str.replace(":","%3A").replace("@","%40").replace("/","%2F");
    }

}