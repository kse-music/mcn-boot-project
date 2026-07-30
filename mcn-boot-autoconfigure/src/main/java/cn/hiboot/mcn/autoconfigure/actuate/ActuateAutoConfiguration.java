package cn.hiboot.mcn.autoconfigure.actuate;

import cn.hiboot.mcn.autoconfigure.actuate.endpoint.McnEndpoint;
import cn.hiboot.mcn.autoconfigure.actuate.health.McnHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * ActuateAutoCofiguration
 *
 * @author DingHao
 * @since 2022/7/22 15:48
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
public class ActuateAutoConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("mcn")
    McnHealthIndicator mcnHealthIndicator(ConfigurableEnvironment environment){
        return new McnHealthIndicator(environment);
    }

    @Bean
    McnEndpoint mcnEndpoint(ConfigurableEnvironment environment){
        return new McnEndpoint(environment);
    }

}
