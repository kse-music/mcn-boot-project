package cn.hiboot.mcn.autoconfigure.bootstrap;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;

/**
 * McnBootstrapRegistryInitializer
 *
 * @author DingHao
 * @since 2021/12/25 16:20
 */
public class McnBootstrapRegistryInitializer implements BootstrapRegistryInitializer {

    @Override
    public void initialize(BootstrapRegistry registry) {
        registry.addCloseListener(event -> {
            BootstrapContext bootstrapContext = event.getBootstrapContext();
            if(bootstrapContext.isRegistered(DuplicateLogFile.class)){
               bootstrapContext.get(DuplicateLogFile.class).check();
            }
        });
    }

}
