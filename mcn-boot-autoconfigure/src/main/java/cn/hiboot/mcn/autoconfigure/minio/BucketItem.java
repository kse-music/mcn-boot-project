package cn.hiboot.mcn.autoconfigure.minio;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/6/28 23:02
 */
public class BucketItem {

    private String name;
    private Date creationDate;

    public BucketItem() {
    }

    public BucketItem(String name, ZonedDateTime creationDate) {
        this.name = name;
        this.creationDate = Date.from(creationDate.toInstant());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
