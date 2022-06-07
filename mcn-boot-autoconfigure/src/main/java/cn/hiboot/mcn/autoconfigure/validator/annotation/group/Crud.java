package cn.hiboot.mcn.autoconfigure.web.validator.group;

/**
 * 不校验默认分组
 *
 * @author DingHao
 * @since 2021/7/26 17:26
 */
public interface Crud {

    interface Create extends Crud{

    }

    interface Update extends Crud{

    }

    interface Query extends Crud{

    }

    interface Delete extends Crud{

    }

}
