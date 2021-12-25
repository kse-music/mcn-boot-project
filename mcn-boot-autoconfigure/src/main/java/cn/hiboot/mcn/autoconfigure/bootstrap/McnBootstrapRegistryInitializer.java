package cn.hiboot.mcn.autoconfigure.bootstrap;

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
        registry.registerIfAbsent(DuplicateLogFile.class, context -> new DuplicateLogFile());
        registry.addCloseListener(event -> {
            DuplicateLogFile duplicateLogFile = event.getBootstrapContext().get(DuplicateLogFile.class);
            duplicateLogFile.setFinalLogFile(event.getApplicationContext().getEnvironment());
            duplicateLogFile.check();
        });
    }

}
