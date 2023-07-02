package cn.hiboot.mcn.core.model.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * BaseBean
 *
 * @author DingHao
 * @since 2021/2/8 17:33
 */
@MappedSuperclass
public abstract class BaseBean {

    /**
     * 創建时间
     */
    @Column(name = "create_at",insertable = false,updatable = false)
    private Date createAt;
    /**
     * 更新时间
     */
    @Column(name = "update_at",insertable = false,updatable = false)
    private Date updateAt;

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
}
