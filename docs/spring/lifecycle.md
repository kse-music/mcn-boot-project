---
description: 当前 Spring Boot 启动事件的时机、监听方式与 MCN 扩展的参与阶段。
---

# 生命周期

本页依据当前依赖的 Spring Boot 4.1.0 `SpringApplication` 与 `EventPublishingRunListener`。六个主要成功启动事件并不等于 Spring 应用只会发布六个事件；上下文、Web 服务器和应用可用性还有各自的事件。

## ApplicationStartingEvent

`run` 开始时发布。Boot 的早期引导上下文已经创建，环境和应用上下文尚未完成准备。

MCN 此时注册日志文件检查器，并在特定依赖存在且没有显式设置时，配置 Spring Security 的上下文持有策略。

## ApplicationEnvironmentPreparedEvent

环境已经创建，并加入命令行等属性源。事件监听器调用环境后处理器，标准配置文件解析以及 MCN 补充属性源加载发生在这一阶段。

同一个事件中的监听器有执行顺序：不能认为排在配置解析之前的监听器已经能读到所有 `application.yml` 属性。

## ApplicationContextInitializedEvent

已创建 `ApplicationContext`，设置环境，并调用初始化器；普通应用配置源尚未加载。事件随后，Boot 关闭早期引导上下文。

MCN 在这里按开关打印属性源，并可处理额外 classpath 配置。属性源打印没有脱敏，默认应保持关闭。

## ApplicationPreparedEvent

应用的主要配置源已加载成 Bean 定义，上下文还没有刷新。当前版本在刷新之前设置循环引用和 Bean 定义覆盖等选项。

MCN 在此将上下文交给 `SpringBeanUtils`。这不代表所有业务 Bean 已实例化，不应在早期监听器中随意触发业务 Bean 的创建。

下面的源码参与文档示例编译，演示提前注册监听器：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/LifecycleExample.java

## ApplicationStartedEvent

`ApplicationContext.refresh()` 已完成，`afterRefresh` 已执行，runner 尚未执行。默认情况下，非懒加载单例已完成初始化；懒加载 Bean 不在这一保证之内。

`ContextRefreshedEvent`、Servlet / Reactive 的 Web 服务器初始化事件可能在此前的刷新过程中出现。它们不应被混同为 `ApplicationStartedEvent`。

## ApplicationReadyEvent

Boot 调用排序后的 `ApplicationRunner` 与 `CommandLineRunner`，全部正常返回后发布 `ApplicationReadyEvent`。`ApplicationRunner` 接收解析后的 `ApplicationArguments`，`CommandLineRunner` 接收原始字符串参数。

Boot 还会发布可用性变化事件，如 `LivenessState.CORRECT` 和 `ReadinessState.ACCEPTING_TRAFFIC`。`ApplicationReadyEvent` 表示 Boot 启动流程已就绪，不能代替业务应用自行定义的远程依赖或数据就绪检查。

## ApplicationFailedEvent

启动过程或 runner 抛出异常时，Boot 尝试报告失败并关闭上下文。此时上下文可能尚未创建、尚未刷新，也可能已经处于活动状态，监听器应兼容这些阶段。

失败事件并不是“成功启动后的固定第七步”。失败监听器本身再次抛出异常，也不会按相同方式递归发布新的失败事件。

## 使用边界

- 早期事件通过 `SpringApplication.addListeners`、构建器或 `spring.factories` 注册监听器。
- 默认事件发布是同步的，监听器中的耗时操作会拖慢对应启动阶段。
- 父子应用上下文可能使同一监听器看到多个同类事件，应按事件所属上下文区分。
- 事件顺序只描述这一启动流程，不保证所有第三方库都在相同阶段完成初始化。

参考：[Spring Boot 官方事件与监听器说明](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.application-events-and-listeners)。
