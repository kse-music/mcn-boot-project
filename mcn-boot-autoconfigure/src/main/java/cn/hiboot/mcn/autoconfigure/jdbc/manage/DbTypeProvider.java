package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DbTypeProvider
 *
 * @author DingHao
 * @since 2025/11/11 9:40
 */
public interface DbTypeProvider extends DbType {

    default String platform() {
        return null;
    }

 }
