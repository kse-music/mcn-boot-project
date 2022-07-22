package cn.hiboot.mcn.autoconfigure.actuate.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * McnHealthIndicator
 *
 * @author DingHao
 * @since 2022/7/22 15:46
 */
public class McnHealthIndicator extends AbstractHealthIndicator {

    private final ConfigurableEnvironment environment;

    public McnHealthIndicator(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.withDetail("mcn.version",environment.getProperty("mcn.version","UN_KNOW")).up();
    }

}
