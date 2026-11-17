---
description: 以当前 Spring Boot 4.1.0 依赖为基线，理解启动扩展点与条件装配。
---

# 核心组件

本章以仓库管理的 Spring Boot 4.1.0 为基线。框架内部类的包名和实现细节可能随版本变化，排查时应使用与依赖版本一致的源码包。

## spring.factories

`META-INF/spring.factories` 仍用于多种启动扩展，不能笼统理解为已经废弃。MCN 当前注册了：

| 扩展点 | MCN 实现 |
| --- | --- |
| `org.springframework.context.ApplicationContextInitializer` | `McnApplicationContextInitializer` |
| `org.springframework.context.ApplicationListener` | `McnApplicationListener` |
| `org.springframework.boot.BootstrapRegistryInitializer` | `McnBootstrapRegistryInitializer` |
| `org.springframework.boot.EnvironmentPostProcessor` | `McnEnvironmentPostProcessor` |
| `org.springframework.boot.autoconfigure.AutoConfigurationImportFilter` | `OnMissingPrimaryDataSource` |
| `org.springframework.boot.diagnostics.FailureAnalyzer` | `SwaggerRunFailureAnalyzer` |

自动配置候选类则列在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中。Spring Cloud 的 bootstrap 配置另外使用 `org.springframework.cloud.bootstrap.BootstrapConfiguration` 键；MCN 的 SM4 配置在此注册。

## ApplicationContextInitializer

在 `ApplicationContext` 已创建、环境已设置、刷新尚未开始时执行。适合注册基础设施后处理器，不能据此认为普通业务 Bean 已可获取。

MCN 在这里注册 `McnBeanFactoryRegistryPostProcessor`。当 `swagger.enabled` 没有启用时，还会追加低优先级属性源，为 `springdoc.api-docs.enabled` 和 `springdoc.swagger-ui.enabled` 提供 `false` 默认值；更高优先级的显式配置仍可能覆盖它。

## ApplicationListener

监听 Spring 事件。部分启动事件发生在容器创建或刷新之前，监听这些早期事件应通过 `SpringApplication.addListeners`、构建器或 `spring.factories` 提前注册，仅声明为普通 Bean 不足以接收已发生的事件。

六个主要成功启动阶段，以及失败与可用性事件，见[生命周期](./lifecycle.md)。

## EnvironmentPostProcessor

Spring Boot 4.1.0 的接口是 `org.springframework.boot.EnvironmentPostProcessor`。它在准备环境时运行，可以添加属性源；执行顺序决定了能否读取此前加载的配置。

当前负责调度环境后处理器的监听器位于 `org.springframework.boot.support.EnvironmentPostProcessorApplicationListener`。MCN 的后处理器在标准 Config Data 处理之后追加补充属性源，具体见[扩展模块](../function/extension.md)。

## ApplicationContextFactory

根据 `WebApplicationType` 创建对应的上下文与环境。当前版本会先调用通过 `spring.factories` 加载的 `WebApplicationType.Deducer` 扩展，再进行 Servlet / 非 Web 的后备判断；不能继续套用旧版本的一段固定 classpath 判断逻辑。

Servlet 的判断使用 `jakarta.servlet.Servlet`，不是旧的 `javax.servlet.Servlet`。Reactive 与 Servlet 混用时应明确选择应用类型及 starter，不要仅凭某个单独类存在来推断最终结果。

## BootstrapContext

Boot 的 `DefaultBootstrapContext` 用于启动早期共享对象，在应用上下文准备完成后关闭。MCN 在其中共享日志文件检查器。

它与 Spring Cloud 为配置解密等创建的父应用上下文不是同一个对象。引入 `spring-cloud-starter-bootstrap` 或明确启用 Cloud bootstrap 的行为，应结合所用 Spring Cloud 版本与配置源说明。

## AutoConfigurationImportFilter

自动配置导入前的快速过滤扩展。Spring Boot 4.1.0 的 `spring.factories` 包含 `OnBeanCondition`、`OnClassCondition` 和 `OnWebApplicationCondition`；MCN 还注册了 `OnMissingPrimaryDataSource`。

常见条件包括：

| 注解 | 判断内容 |
| --- | --- |
| `@ConditionalOnClass` / `@ConditionalOnMissingClass` | 依赖类是否存在 |
| `@ConditionalOnBean` / `@ConditionalOnMissingBean` | 容器中是否已存在匹配的 Bean 定义 |
| `@ConditionalOnSingleCandidate` | 是否能确定单个候选 Bean，含 primary 选择 |
| `@ConditionalOnProperty` | 属性是否满足 `havingValue`、`matchIfMissing` 等条件 |
| `@ConditionalOnWebApplication` / `@ConditionalOnNotWebApplication` | 应用类型 |
| `@ConditionalOnResource` | 资源是否存在 |
| `@ConditionalOnExpression` | SpEL 表达式结果 |
| `@ConditionalOnWarDeployment` | 是否为外部 Servlet 容器部署 |

导入前过滤只能依据当时可用的类型信息与自动配置元数据做初步判断。后续条件评估还会结合 Bean 定义等信息；不要把“候选类通过快速过滤”当作“配置中的所有 Bean 都已创建”。

MCN 的数据源过滤器检查 `spring.datasource.url` 是否可绑定：缺失时排除 Boot 的 `DataSourceAutoConfiguration`、`HibernateJpaAutoConfiguration` 和 `DataJpaRepositoriesAutoConfiguration`，为 MCN 多数据源接入让出位置。仅自定义 DataSource Bean、却不配置该 URL 时，也应检查这个过滤器的实际影响。

## 源码与参考

- [MCN 启动扩展注册](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/resources/META-INF/spring.factories)
- [MCN 自动配置清单](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)
- [Spring Boot 自动配置参考](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
