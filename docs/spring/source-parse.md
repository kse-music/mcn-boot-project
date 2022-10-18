# 源码解读

## 配置解析

从生命周期章节我们知道配置文件的解析是发生在EnvironmentPostProcessorApplicationListener执行环境后置处理器的过程中，通过从spring.factories中获取所有实现了EnvironmentPostProcessor接口的配置类，如下：

1. org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor
2. org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor
3. org.springframework.boot.env.RandomValuePropertySourceEnvironmentPostProcessor
4. org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor
5. org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor
6. org.springframework.boot.reactor.DebugAgentEnvironmentPostProcessor
7. org.springframework.boot.autoconfigure.integration.IntegrationPropertiesEnvironmentPostProcessor

实例化并排序后顺序如下：

1. **org.springframework.boot.env.RandomValuePropertySourceEnvironmentPostProcessor**
* 添加一个名为random的属性源到Environment中
* 通过诸如abc=${random.int(11,22)}配置，表示随机一个整数在11至22之间
* 底层直接使用Random类实现

2. **org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor**
* 将SystemEnvironmentPropertySource替换为OriginAwareSystemEnvironmentPropertySource
* 用于追踪不同系统的环境属性

3. **org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor**
* 解析属性名为spring.application.json或SPRING_APPLICATION_JSON的json字符串为Map<String,Object>
* 解析json的工具顺序是：Jackson > Gson > Yaml，以上都没有则使用内置的BasicJsonParser
* 如果系统存在StandardServletEnvironment则将json属性源放在Servlet属性源前面
* 如果系统不存在StandardServletEnvironment则将json属性源放在Environment中第一个位置即优先级最高

4. org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor

5. **org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor**
* ConfigDataLocationResolver：将location解析成ConfigDataResource
* ConfigDataLoader：通过给定的ConfigDataResource去加载ConfigData
* ConfigDataProperties：记录配置解析过程中额外导入的配置和激活的云平台以及激活的profile
* processInitial：初始处理文件（无activationContext）
* processWithoutProfiles：处理配置数据环境（初始的activationContext）
* processWithProfiles：最后处理配置数据环境（已激活的activationContext）

6. org.springframework.boot.reactor.DebugAgentEnvironmentPostProcessor
7. org.springframework.boot.autoconfigure.integration.IntegrationPropertiesEnvironmentPostProcessor

::: tip 重要
ConfigDataLocationResolver与ConfigDataLoader的实现都是从spring.factories中获取，言外之意就是我们也可以自己定制自己的外部配置数据，只需要有对应的ConfigDataLoader。如springc-cloud-config中的ConfigServerConfigDataLocationResolver与ConfigServerConfigDataLoader。

配置文件一般存放在两个位置：

1. 文件目录下，优先级从低到高

<img :src="$withBase('/images/app-prop1.png')">

2. classpath下，优先级从低到高

<img :src="$withBase('/images/app-prop2.png')">

:::

::: warning 注意
1. **相同配置项文件目录中的优先级比classpath高**
:::

最终Environment中属性源顺序如下图所示：

<img :src="$withBase('/images/env.png')" alt="env">

## 日志系统

SpringBoot内置三种日志系统：

1. LogbackLoggingSystem
2. Log4J2LoggingSystem
3. JavaLoggingSystem

日志系统的初始化由LoggingApplicationListener完成，有以下5个过程：

1. ApplicationStartingEvent：应用刚启动时主要为初始化日志系统做准备
* 可以通过配置系统属性org.springframework.boot.logging.LoggingSystem=none关闭日志系统
* 如果org.springframework.boot.logging.LoggingSystem对应的值是继承了抽象类LoggingSystem的className则会使用自实现的日志系统
* 前两种情况一般使用不到，所以系统默认就会从内置的三种日志系统选择一个，这里我们得到的是LogbackLoggingSystem
* 执行beforeInitialize，如果存在SLF4JBridge等依赖，就会把对应的日志全部桥接到统一的日志输出，如：jul-to-slf4j

2. ApplicationEnvironmentPreparedEvent：开始初始化日志系统
* 将指定存在于Environment中的配置项(如：编码、样式、级别等)设置到系统属性中
* 通过配置项logging.file.name或logging.file.path获取日志文件(注意：**同时设置可不是两者的组合**)
* 如果通过logging.config指定了日志的配置文件(如：logback.xml)则会使用指定的配置文件初始化，默认未指定
* 接下来会默认从classpath下查找是否存在logback-test.groovy、logback-test.xml、logback.groovy、logback.xml文件，如果还没有则会将前面四类文件后拼接-spring后继续在classpath下查找即查找logback-test-spring.groovy、logback-test-spring.xml、logback-spring.groovy、logback-spring.xml是否存在
* 由于我们什么都没配置最后SpringBoot通过编程方式执行默认日志配置初始化(DefaultLogbackConfiguration)完成后并标记已初始化

3. ApplicationPreparedEvent：将日志系统相关对象注册到IOC容器中
* 如果不存在名为springBootLoggingSystem的Bean则把loggingSystem注册到容器中
* 如果配置了日志文件且容器中不存在名为springBootLogFile的Bean则把logFile注册到容器中
* 如果配置了日志组且容器中不存在名为springBootLoggerGroups的Bean则把loggerGroups注册到容器中

4. ContextClosedEvent：发生上下文关闭事件，执行清理流程
* 应用上下文关闭则会清理日志系统，如标记日志系统未初始化等操作

5. ApplicationFailedEvent：应用启动失败，执行清理流程
* SpringBoot应用启动失败也会清理日志系统

## 加载Starters

主要过程：

1. 加载
* 读取META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports文件所有自动配置类,默认有144个
* 通过注解SpringBootApplication中定义的exclude和excludeName以及配置文件中spring.autoconfigure.exclude配置项获取需要排除的自动配置类
* 根据前面核心组件中的[三个条件接口](core.md#AutoConfigurationImportFilter)生成ConfigurationClassFilter(同时加载自动配置元数据[845项])用于过滤自动配置类，最终只剩下24项

2. 排序
* 首选将自动配置类的before和after注解相关的 **可用类**（存在classpath中）添加到结果集（34项）上
* 接着先按字母顺序自然排序
* 再按指定的自动配置顺序（AutoConfigureOrder,默认0）排
* 最后再根据before和after指定依赖排序

::: tip 重要

1. **在过滤自动配置类时如果前期没有生成自动配置元数据则不能在此时过滤**
2. **要指定自动配置类的配置顺序必须用AutoConfigureOrder不能使用Ordered或者Order注解**
3. OnClassCondition处理ConditionalOnClass注解
4. OnWebApplicationCondition处理ConditionalOnWebApplication注解
5. OnBeanCondition处理ConditionalOnBean和ConditionalOnSingleCandidate注解且只是简单判断bean的类型是否存在即class是否存在

元数据生成插件：

1. spring-boot-autoconfigure-processor：处理自动配类的注解（8个）输出记录在META-INF下spring-autoconfigure-metadata.properties包括条件注解、配置顺序及前后依赖等注解方便后续那来即用不需要实时解析
* org.springframework.boot.autoconfigure.condition.ConditionalOnClass
* org.springframework.boot.autoconfigure.condition.ConditionalOnBean
* org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
* org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
* org.springframework.boot.autoconfigure.AutoConfigureBefore
* org.springframework.boot.autoconfigure.AutoConfigureAfter
* org.springframework.boot.autoconfigure.AutoConfigureOrder
* org.springframework.boot.autoconfigure.AutoConfiguration

2. spring-boot-configuration-processor：处理自动配置类所依赖的配置属性并输出记录在META-INF下spring-configuration-metadata.json文件中，后续开发时在IDE中就可以提示配置项的类型和默认值（如果有）

:::