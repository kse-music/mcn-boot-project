package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * FilterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/19 17:59
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(NameValueProcessorJacksonConfig.class)
@EnableConfigurationProperties(WebSecurityProperties.class)
public class FilterAutoConfiguration {

}
