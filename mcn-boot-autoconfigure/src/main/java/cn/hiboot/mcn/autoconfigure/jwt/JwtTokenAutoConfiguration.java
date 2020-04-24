package cn.hiboot.mcn.autoconfigure.jwt;

import com.auth0.jwt.JWT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JWT.class)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name={"jwtToken"})
    public JwtToken jwtToken(JwtProperties jwtProperties) {
        return new JwtToken(jwtProperties);
    }

}
