package cn.hiboot.mcn.autoconfigure.web.groovy;

import groovy.lang.Binding;

/**
 * BindingCustomizer
 *
 * @author DingHao
 * @since 2021/12/22 15:35
 */
public interface BindingCustomizer {
    void customize(Binding binding);
}
