package cn.hiboot.mcn.cloud.security.configurer;

import java.util.Map;

/**
 * AuthenticationReload
 *
 * @author DingHao
 * @since 2023/1/16 12:18
 */
public interface AuthenticationReload {

    Map<String, Object> reload(Map<String, Object> principal);

}
