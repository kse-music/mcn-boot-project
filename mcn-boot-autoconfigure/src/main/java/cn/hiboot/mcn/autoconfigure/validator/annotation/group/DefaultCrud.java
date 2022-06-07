package cn.hiboot.mcn.autoconfigure.web.validator.group;

/**
 * 也校验默认分组
 *
 * @author DingHao
 * @since 2021/7/26 18:20
 */
public interface DefaultCrud extends ValidGroup{

    interface Create extends DefaultCrud{

    }

    interface Update extends DefaultCrud{

    }

    interface Query extends DefaultCrud{

    }

    interface Delete extends DefaultCrud{

    }

}
