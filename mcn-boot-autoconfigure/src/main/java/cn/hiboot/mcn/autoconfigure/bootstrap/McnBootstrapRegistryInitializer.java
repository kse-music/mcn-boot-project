package cn.hiboot.mcn.autoconfigure.bootstrap;


import org.springframework.boot.bootstrap.BootstrapContext;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;

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
            if(bootstrapContext.isRegistered(LogFileChecker.class)){
               bootstrapContext.get(LogFileChecker.class).check();
            }
        });
    }

}
