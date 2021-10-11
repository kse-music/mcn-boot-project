package cn.hiboot.mcn.core.model.base;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @Column(name = "create_at",insertable = false,updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(name = "update_at",insertable = false,updatable = false)
    private Date updateAt;

}
