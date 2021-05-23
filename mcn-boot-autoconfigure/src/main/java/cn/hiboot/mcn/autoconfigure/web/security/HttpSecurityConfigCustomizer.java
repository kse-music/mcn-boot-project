package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * HttpSecurityConfigCustomizer
 *
 * @author DingHao
 * @since 2021/5/23 23:40
 */
public interface HttpSecurityConfigCustomizer {

    void customize(HttpSecurity http) throws Exception;

}
