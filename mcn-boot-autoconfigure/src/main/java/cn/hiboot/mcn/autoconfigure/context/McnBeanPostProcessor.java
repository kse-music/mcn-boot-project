package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *
 * BeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor{

    private Environment environment;

    public McnBeanPostProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper){
            JacksonUtils.setObjectMapper((ObjectMapper) bean);
        }else if(bean instanceof MongoProperties){
            String mongoAddress = environment.getProperty("mongo.addrs");
            if(ObjectUtils.isEmpty(mongoAddress)){
                mongoAddress = "127.0.0.1:27017";
            }
            MongoProperties mongoProperties = (MongoProperties) bean;
            String username = mongoProperties.getUsername();
            String password = environment.getProperty("mongo.password");
            String uri = mongoAddress;
            if(StringUtils.hasText(username) && StringUtils.hasText(password)){
                uri = replace(username)+":" + replace(password) + "@" + mongoAddress;
            }
            mongoProperties.setUri("mongodb://"+uri);
        }else if(bean instanceof RedisProperties){
            RedisProperties redisProperties = (RedisProperties) bean;
            String master = redisProperties.getSentinel().getMaster();
            List<String> hosts = redisProperties.getCluster().getNodes();
            if(StringUtils.hasText(master)){//哨兵
                redisProperties.getSentinel().setNodes(hosts);
                redisProperties.setCluster(null);
            }else {//cluster
                redisProperties.setSentinel(null);
                if(hosts.size() == 1){//单机
                    String[] hp = hosts.get(0).split(":");
                    redisProperties.setHost(hp[0]);
                    redisProperties.setPort(Integer.parseInt(hp[1]));
                    redisProperties.setCluster(null);
                }
            }
        }
        return bean;
    }

    private String replace(String str){
        return str.replace(":","%3A").replace("@","%40").replace("/","%2F");
    }

}