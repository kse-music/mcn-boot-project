package cn.hiboot.mcn.autoconfigure.web.swagger;

import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * DocketCustomizer
 *
 * @author DingHao
 * @since 2022/4/21 20:42
 */
public interface DocketCustomizer {

    void customize(Docket docket);

    interface SelectBuilder extends DocketCustomizer{

        @Override
        default void customize(Docket docket) {

        }

        void customize(ApiSelectorBuilder select);

    }

}