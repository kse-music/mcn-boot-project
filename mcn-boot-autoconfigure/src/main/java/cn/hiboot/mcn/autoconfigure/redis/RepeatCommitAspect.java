package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.redis.annotation.RepeatCommit;
import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.util.JacksonUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ApiRepeatCommitAspect
 *
 * @author DingHao
 * @since 2021/10/15 11:22
 */
@Aspect
public class RepeatCommitAspect {

    private final static String REQUEST_HASH_PREFIX = "request:hash:";

    private final StringRedisTemplate redisTemplate;

    public RepeatCommitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Pointcut("@annotation(repeatCommit)")
    public void pointCut(RepeatCommit repeatCommit) {
    }

    @Around(value = "pointCut(repeatCommit)", argNames = "p,repeatCommit")
    public Object around(ProceedingJoinPoint p, RepeatCommit repeatCommit)  throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null){
            HttpServletRequest request = requestAttributes.getRequest();
            StringBuilder requestContent = new StringBuilder();
            requestContent.append(request.getMethod());
            requestContent.append(request.getServletPath());
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!CollectionUtils.isEmpty(parameterMap)) {
                requestContent.append(JacksonUtils.toJson(parameterMap));
            }
            if(JsonRequestHelper.isJsonRequest(request)){
                requestContent.append(StreamUtils.copyToString(request.getInputStream(),StandardCharsets.UTF_8));
            }
            String hash = DigestUtils.md5DigestAsHex(requestContent.toString().getBytes(StandardCharsets.UTF_8));
            String key = REQUEST_HASH_PREFIX + hash;
            Boolean status = redisTemplate.opsForValue().setIfAbsent(key, key, repeatCommit.value(), TimeUnit.MILLISECONDS);
            if (status == null || !status) {
                throw ServiceException.newInstance("重复提交");
            }
        }
        return p.proceed();
    }

}
