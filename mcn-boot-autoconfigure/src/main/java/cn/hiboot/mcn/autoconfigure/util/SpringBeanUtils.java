package cn.hiboot.mcn.autoconfigure.util;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * SpringBeanUtils
 * 当存在多个上下文时且当前上下文是子上下文时会更新applicationContext
 *
 * @author DingHao
 * @since 2021/6/30 15:40
 */
public class SpringBeanUtils{
	
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
		Assert.notNull(context,"applicationContext not inject yet");
		return context;
	}

}
