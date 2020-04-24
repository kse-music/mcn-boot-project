package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.core.model.result.RestResp;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:06
 */
@Aspect
public class DurationAop {

    @SuppressWarnings("rawtypes")
    @Around("@annotation(cn.hiboot.mcn.autoconfigure.web.filter.Timing)")
    public Object timeRecord(ProceedingJoinPoint p) throws Throwable {
        long s = System.currentTimeMillis();
        Object o = p.proceed();
        if(o instanceof RestResp){
            RestResp r = (RestResp) o;
            r.setDuration(System.currentTimeMillis()-s);
        }
        return o;
    }

}
