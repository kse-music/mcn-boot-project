# 生命周期

Springboot应用在正常启动过程会触发六个事件，失败则会触发ApplicationFailedEvent事件，可以通过实现监听器来处理感兴趣的事件。


## ApplicationStartingEvent

标识SpringApplication正在启动，此时已实例化出一个DefaultBootstrapContext以及早期的事件发布器EventPublishingRunListener，随后发布ApplicationStartingEvent，并携带前面的bootstrapContext实例和正在启动的SpringApplication实例。

::: tip 提示

实现接口BootstrapRegistryInitializer的配置和EventPublishingRunListener都是从spring.factories中获取

:::

## ApplicationEnvironmentPreparedEvent

Environment实例化过程：

1. 通过不同的webApplicationType去实例化对应的Environment
2. 给Environment添加转换服务
3. 解析命令行参数并添加到Environment中

随后发布ApplicationEnvironmentPreparedEvent事件同时携带bootstrapContext实例和SpringApplication实例以及刚实例化的Environment。

::: tip 提示

其中核心配置文件application.properties的解析就是在EnvironmentPostProcessorApplicationListener监听器中通过执行EnvironmentPostProcessor即 **ConfigDataEnvironmentPostProcessor** 实现的

:::

## ApplicationContextInitializedEvent

ApplicationContext实例化过程：

1. 首选通过ApplicationContextFactory创建出对应的ApplicationContext
2. 把Environment设置到上下文中
3. 执行ApplicationContextInitializer

随后发布ApplicationContextInitializedEvent事件同时携带前面的ApplicationContext实例和SpringApplication实例。

::: tip 提示

实现接口ApplicationContextInitializer和ApplicationListener的配置都是从spring.factories中获取，另外由于此时已经有了ApplicationContext了，所以随后就立即关闭了bootstrapContext并发送BootstrapContextClosedEvent事件

:::

## ApplicationPreparedEvent

上下文配置以及主配置加载过程：

1. **从2.6.0开始默认关闭bean循环引用的支持**
2. 注册主配置（即示例中的DemoApplication）到IOC容器中中

随后发布ApplicationPreparedEvent事件并将SpringApplication的监听器全部加入ApplicationContext中，并携带ApplicationContext实例和SpringApplication实例。

::: tip 提示

主配置有四种形式：

1. Class
2. Resource：可以是groovy或者xml
3. Package：包对象
4. CharSequence：可以是变量（如${...}）
* 当作Class
* 当作Resource
* 当作包路径

:::

## ApplicationStartedEvent

1. 注册关闭钩子用于在JVM关闭时优雅的关闭SpringApplication（清理资源、销毁对象等）
2. **ApplicationContext刷新**（解析BeanDefinition并实例化单例对象）
3. 打印应用启动时间

随后发布ApplicationStartedEvent事件同时携带ApplicationContext实例和SpringApplication实例以及启动耗时时间。

::: tip 提示

ApplicationContext上下文的刷新意味着所有的单例bean已实例化并存储到IOC容器，注意此时的上下文已经准备完毕，所以ApplicationStartedEvent事件是由ApplicationContext中的事件发布器发布的

:::

## ApplicationReadyEvent

执行实现Runner接口的bean，有两种接口：

1. ApplicationRunner
2. CommandLineRunner

随后发布ApplicationReadyEvent事件同时携带ApplicationContext实例和SpringApplication实例以及从启动到执行Runner耗时时间。

::: tip 提示

两种接口的区别：

CommandLineRunner使用的原始参数即args，而ApplicationRunner是将args转成内部标准的参数结构即ApplicationArguments

:::

## ApplicationFailedEvent

两种情况：

1. 应用启动过程中失败
2. 执行发布ApplicationFailedEvent事件异常


::: tip 提示

第一种情况会在异常后发布ApplicationFailedEvent事件同时携带ApplicationContext实例和exception对象

第二种情况会则不会发送ApplicationFailedEvent事件

:::