package cn.hiboot.mcn.autoconfigure.actuate.endpoint;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.function.Function;

/**
 * McnEndpoint
 *
 * @author DingHao
 * @since 2022/7/22 15:52
 */
@Endpoint(id = "mcn")
public class McnEndpoint {

    private final ConfigurableEnvironment environment;

    public McnEndpoint(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @ReadOperation
    public RestResp<String> version() {
        return RestResp.ok(environment.getProperty("mcn.version", "UN_KNOW"));
    }

    @ReadOperation
    public RestResp<String> get(@Selector String name) {
        return RestResp.ok(environment.getProperty(name));
    }

    @WriteOperation
    public RestResp<Object> add(String name, Object value) {
        return RestResp.ok(doAction(mapPropertySource -> mapPropertySource.getSource().put(name, value)));
    }

    private Object doAction(Function<MapPropertySource, Object> function) {
        MapPropertySource mapPropertySource = (MapPropertySource) environment.getPropertySources().get(ConfigProperties.MCN_MAP_PROPERTY_SOURCE_NAME);
        return function.apply(mapPropertySource);
    }

    @DeleteOperation
    public RestResp<Object> delete(@Selector String name) {
        return RestResp.ok(doAction(mapPropertySource -> mapPropertySource.getSource().remove(name)));
    }

}
