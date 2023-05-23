package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.core.exception.BaseException;

/**
 * DataIntegrityException
 *
 * @author DingHao
 * @since 2023/5/23 14:39
 */
public class DataIntegrityException extends BaseException {

    private DataIntegrityException(String msg) {
        super(msg);
    }

    public static DataIntegrityException newInstance(String msg){
        return new DataIntegrityException(msg);
    }

}
