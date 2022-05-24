package cn.hiboot.mcn.core.util;

import org.springframework.context.ApplicationContext;

/**
 * SpringBeanUtils
 * 当存在多个上下文时且当前上下文是子上下文时会更新applicationContext
 *
 * @author DingHao
 * @since 2021/6/30 15:40
 */
public abstract class SpringBeanUtils{
	
	private static volatile ApplicationContext context;

	public static Object getBean(String name) {  
		return getApplicationContext().getBean(name);
	}  
	
	public static <T> T getBean(String name,Class<T> clazz) {
		return getApplicationContext().getBean(name,clazz);
	} 
	
	public static <T> T getBean(Class<T> clazz) {  
		return getApplicationContext().getBean(clazz);
	}

	public static void setApplicationContext(ApplicationContext applicationContext) {
		if(context == null || applicationContext.getParent() != null){//if context is child
			context = applicationContext;
		}
	}

	public static ApplicationContext getApplicationContext() {
		McnAssert.notNull(context,"applicationContext not inject yet");
		return context;
	}

}
