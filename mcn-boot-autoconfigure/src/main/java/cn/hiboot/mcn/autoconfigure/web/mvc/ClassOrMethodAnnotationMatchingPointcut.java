package cn.hiboot.mcn.autoconfigure.web.mvc;

import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * ClassOrMethodAnnotationMatchingPointcut
 *
 * @author DingHao
 * @since 2022/7/13 21:20
 */
public class ClassOrMethodAnnotationMatchingPointcut extends AnnotationMatchingPointcut {

    private final MethodMatcher methodMatcher;

    public ClassOrMethodAnnotationMatchingPointcut(Class<? extends Annotation> annotationType) {
        super(null,annotationType);
        AnnotationClassFilter annotationClassFilter = new AnnotationClassFilter(annotationType);
        this.methodMatcher = new AnnotationMethodMatcher(annotationType){
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return annotationClassFilter.matches(targetClass) || super.matches(method, targetClass);
            }
        };
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

}
