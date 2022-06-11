package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.result.RestResp;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设置并打印接口执行时间
 *
 * @author DingHao
 * @since 2019/7/13 11:06
 */
@Aspect
public class DurationAop {

    private static final Logger log = LoggerFactory.getLogger(DurationAop.class);

    @SuppressWarnings("rawtypes")
    @Around("@annotation(cn.hiboot.mcn.autoconfigure.web.mvc.Timing)")
    public Object timeRecord(ProceedingJoinPoint p) throws Throwable {
        long s = System.currentTimeMillis();
        Object o = p.proceed();
        long duration = System.currentTimeMillis() - s;
        if(o instanceof RestResp){
            RestResp r = (RestResp) o;
            r.setDuration(duration);
        }
        log.info("execute took {} ms",duration);
        return o;
    }

}
