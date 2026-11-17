package cn.hiboot.mcn.docs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

public class LifecycleExample {
    public static SpringApplication application(Class<?> source) {
        SpringApplication application = new SpringApplication(source);
        application.addListeners(new PreparedListener());
        return application; // 调用方再执行 application.run(args)。
    }

    static class PreparedListener implements ApplicationListener<ApplicationPreparedEvent> {
        @Override
        public void onApplicationEvent(ApplicationPreparedEvent event) {
            // 此时 Bean 定义已加载，但 ApplicationContext 尚未刷新。
            // 不在这里获取尚未实例化的业务 Bean。
        }
    }
}
