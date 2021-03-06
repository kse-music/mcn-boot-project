package cn.hiboot.mcn.autoconfigure.web.mvc;

import springfox.documentation.spring.web.plugins.Docket;

/**
 * Docket Customizer
 *
 * @author DingHao
 * @since 2020/2/11 20:54
 */
public interface DocketCustomizer {
    void customize(Docket docket);
}
