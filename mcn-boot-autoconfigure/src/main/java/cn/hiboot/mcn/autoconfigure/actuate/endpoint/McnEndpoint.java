package cn.hiboot.mcn.autoconfigure.actuate.endpoint;

import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

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
        return new RestResp<>(environment.getProperty("mcn.version","UN_KNOW"));
    }

    @ReadOperation
    public RestResp<String> get(@Selector String name) {
        return new RestResp<>(environment.getProperty(name));
    }

    @WriteOperation
    public RestResp<?> add(String name,Object value) {
        MapPropertySource mapPropertySource = (MapPropertySource) environment.getPropertySources().get("mcn-map");
        mapPropertySource.getSource().put(name,value);
        return new RestResp<>();
    }

    @DeleteOperation
    public RestResp<?> delete(@Selector String name) {
        MapPropertySource mapPropertySource = (MapPropertySource) environment.getPropertySources().get("mcn-map");
        return new RestResp<>(mapPropertySource.getSource().remove(name));
    }

}
