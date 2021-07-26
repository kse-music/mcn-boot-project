package cn.hiboot.mcn.autoconfigure.web.validator.group;

import javax.validation.groups.Default;

/**
 * 如果继承了Default，@Validated标注的注解也会校验未指定分组或者Default分组的参数，比如email
 *
 * 如果不继承Default则不会校验未指定分组的参数，
 * 需要加上@Validated(value = {ValidGroup.Crud.Update.class, Default.class}才会校验
 *
 * @author DingHao
 * @since 2021/7/26 17:25
 */
public interface ValidGroup extends Default {

}
