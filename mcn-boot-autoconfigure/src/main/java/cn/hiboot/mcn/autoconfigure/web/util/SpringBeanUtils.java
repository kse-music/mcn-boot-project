package cn.hiboot.mcn.autoconfigure.web.util;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class SpringBeanUtils{
	
	private static ApplicationContext applicationContext;

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
		if(SpringBeanUtils.applicationContext == null || applicationContext.getParent() != null){//if child set child
			SpringBeanUtils.applicationContext = applicationContext;
		}
	}

	public static ApplicationContext getApplicationContext() {
		Assert.notNull(applicationContext,"applicationContext not inject yet");
		return applicationContext;
	}

}
