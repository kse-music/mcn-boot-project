package cn.hiboot.mcn.cloud.security.token;

/**
 * LoginRsp
 *
 * @author DingHao
 * @since 2023/2/15 22:29
 */
public class LoginRsp {
    private String token;
    private boolean admin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
