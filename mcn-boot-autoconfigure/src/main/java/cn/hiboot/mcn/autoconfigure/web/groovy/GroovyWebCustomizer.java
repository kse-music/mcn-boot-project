package cn.hiboot.mcn.autoconfigure.web.groovy;

import groovy.lang.Binding;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * GroovyWebCustomizer
 *
 * @author DingHao
 * @since 2021/12/22 15:35
 */
public interface GroovyWebCustomizer {

    void customize(Binding binding,CompilerConfiguration configuration);

}
