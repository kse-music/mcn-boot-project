package cn.hiboot.mcn.autoconfigure.validator.group;

import javax.validation.groups.Default;

/**
 * <p>如果继承了Default，@Validated标注的注解也会校验未指定分组或者Default分组的参数，比如email</p>
 *
 * <p>如果不继承Default则不会校验未指定分组的参数，
 * 需要加上@Validated(value = {ValidGroup.Crud.Update.class, Default.class}才会校验</p>
 *
 * <p>GroupSequence注解可以指定校验顺序</p>
 *
 * @author DingHao
 * @since 2021/7/26 17:25
 */
public interface ValidGroup extends Default {

}
