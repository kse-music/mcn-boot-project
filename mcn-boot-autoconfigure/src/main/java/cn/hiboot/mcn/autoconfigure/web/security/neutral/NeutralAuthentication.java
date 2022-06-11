package cn.hiboot.mcn.autoconfigure.web.security.neutral;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * NeutralAuthentication
 *
 * @author DingHao
 * @since 2022/6/12 0:09
 */
public class NeutralAuthentication extends AbstractAuthenticationToken {

    private static final String TOKEN = "NeutralToken";

    public NeutralAuthentication() {
        super(Collections.emptyList());
        setAuthenticated(true);
    }

    public String getToken() {
        return TOKEN;
    }

    @Override
    public Object getCredentials() {
        return this.getToken();
    }

    @Override
    public Object getPrincipal() {
        return this.getToken();
    }

}