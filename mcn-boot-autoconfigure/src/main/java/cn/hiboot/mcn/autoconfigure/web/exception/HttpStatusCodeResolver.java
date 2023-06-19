package cn.hiboot.mcn.autoconfigure.web.exception;

/**
 * HttpStatusCodeResolver
 *
 * @author DingHao
 * @since 2023/6/17 23:02
 */
public interface HttpStatusCodeResolver {
    Integer resolve(Throwable ex);
}
