# 核心组件

SpringBoot是一个约定大于配置的框架。例如当你引入了springmvc依赖时，系统就认为你需要开发基于springmvc的web项目。

## spring.factories

核心工厂配置文件，其中包括日志系统、属性源加载器、配置数据路径解析器以及配置数据加载器、应用上下文配置(Servlet或Reactive)等等。

::: tip 提示

从2.7.0开始自动配置类被移到专门的文件位于META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports,并使用新的注解@AutoConfiguration标识这是一个自动配置类

:::

## ApplicationContextInitializer

应用上下文初始化器在IOC刷新前(也就是常规的BeanDefinition未加载时、bean也未实例化)执行,这里主要用编程的方式去初始化一些程序需要的东西，比如加载外部属性源配置、激活环境的profile等。

::: tip 提示

nacos配置中心就是基于这个实现的，apollo不是。SpringCloud提供的标准PropertySourceBootstrapConfiguration去加载外部属性源，而该初始化器是只在引导上下文中启用，这也解释了为什么使用nacos必须启用引导上下文。

:::

::: warning 注意

从2.4.0版本开始,默认不启动引导上下文，可通过指定系统属性变量或引入spring-cloud-starter-bootstrap依赖启用

:::

## ApplicationListener

在Springboot生命周期中会发出七个事件,正常情况会触发六个，启动失败会触发失败事件，应用可以自定义若干监听器去处理感兴趣的事件。具体后续详细介绍。

## EnvironmentPostProcessor

ApplicationContextInitializer和ApplicationListener在spring中就存在了，而EnvironmentPostProcessor是springboot特有的，主要用作在Environment可用时执行一些配置的解析并添加到属性源中，最核心的就是 **ConfigDataEnvironmentPostProcessor**，application.properties就是在此解析。


## ApplicationContextFactory

通过classpath下不同依赖去初始化对应的上下文：

1. 只有DispatcherHandler没有DispatcherServlet和ServletContainer(jersey的servlet实现)，则对应的上下文是AnnotationConfigReactiveWebServerApplicationContext
2. 当javax.servlet.Servlet和ConfigurableWebApplicationContext存在时，则对应的上下文是AnnotationConfigServletWebServerApplicationContext
3. 如果以上都不是系统默认使用AnnotationConfigApplicationContext上下文（基于springboot的脚本开发）

## BootstrapContext

2.4.0新增，一个简单的引导上下文，在启动和环境后处理期间可用，直到准备好 ApplicationContext。
提供对单例的惰性访问，这些单例的创建成本可能很高，或者需要在 ApplicationContext 可用之前共享。

::: warning 注意

注意区分与SpringCloud创建的引导上下文，后者创建AnnotationConfigApplicationContext上下文并作为应用的父上下文

:::

## AutoConfigurationImportFilter

自动导入过滤器，在加载所有Starter时执行。底层是通过Spring4新增的Condition接口实现。

Condition条件初始化核心接口,简单说就是当满足指定条件时就去注册一个bean或者不注册一个bean，而基于该接口SpringBoot实现了很多方便使用的注解，当然我们也可以自定义，常见的有：

1. <font color=red>ConditionalOnBean</font>：当存在某些bean时，发生在加载BeanDefinition阶段
2. <font color=red>ConditionalOnClass</font>：当存在某些class时
3. ConditionalOnExpression：当评估一个SPEL表达式为true时
4. <font color=red>ConditionalOnMissingBean</font>：当不存在某些bean时，发生在加载BeanDefinition阶段
5. <font color=red>ConditionalOnMissingClass</font>：当不存在某些class时
6. <font color=red>ConditionalOnNotWebApplication</font>：当应用是非web时
7. ConditionalOnProperty：当存在某些指定属性值时
8. ConditionalOnResource：当存在某些资源时
9. <font color=red>ConditionalOnSingleCandidate</font>：当IOC容器只存在一个候选bean时，发生在加载BeanDefinition阶段
10. ConditionalOnWarDeployment：当应用是war包部署时
11. <font color=red>ConditionalOnWebApplication</font>：当应用是web(Servlet或Reactive)时

::: warning 注意

标红的部分是在加载Starter时就开始过滤（因为只有以下三个实现了AutoConfigurationImportFilter接口）自动配置类，其中

* 2、5对应的实现是OnClassCondition
* 6、11对应的实现是OnWebApplicationCondition
* 1、4、9对应的实现是OnBeanCond

:::