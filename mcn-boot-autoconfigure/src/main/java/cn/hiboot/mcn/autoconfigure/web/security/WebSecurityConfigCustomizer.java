package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * WebSecurityConfigCustomizer
 *
 * @author DingHao
 * @since 2021/5/23 23:40
 */
public interface WebSecurityConfigCustomizer {

    void customize(WebSecurity web) throws Exception;

}