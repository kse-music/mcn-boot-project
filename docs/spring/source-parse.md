---
description: 以当前依赖源码理解配置解析、日志初始化和自动配置导入。
---

# 源码解读

这里解释当前 Spring Boot 4.1.0 与 MCN 的相关调用链。自动配置数量、元数据条数和最终 Bean 数量取决于应用依赖和配置，不应使用某次调试结果作为固定规则。

## 配置解析

主要调用链如下：

```text
SpringApplication.run
  → prepareEnvironment
  → 发布 ApplicationEnvironmentPreparedEvent
  → EnvironmentPostProcessorApplicationListener
  → 按顺序执行 EnvironmentPostProcessor
      → ConfigDataEnvironmentPostProcessor
      → McnEnvironmentPostProcessor
```

Boot 的后处理器除 Config Data 外，还可能处理系统环境、随机值、`SPRING_APPLICATION_JSON` 等。具体参与者由当前 classpath 中的 `spring.factories` 决定；Spring Boot 4.1 的部分实现已迁移到 `org.springframework.boot.support` 包，旧版本的类名列表不能直接当作当前清单。

### Config Data

`ConfigDataLocationResolver` 将配置位置解析为资源，`ConfigDataLoader` 负责加载配置数据。两类扩展通过对应的启动扩展注册机制参与流程，可用于远程配置或自定义资源协议。

常规配置数据位置的优先级，从低到高，可概括为：

1. jar 内的 `application.properties` / `application.yml`。
2. jar 内对应 profile 的配置。
3. jar 外的常规配置。
4. jar 外对应 profile 的配置。

这只是 Config Data 这一部分的顺序。`spring.config.location` 会替换默认搜索位置，`spring.config.additional-location` 用于补充位置，`spring.config.import` 还会影响导入关系；命令行、系统属性等属于更完整的属性源优先级体系。

### JSON 与随机属性

`spring.application.json` 或 `SPRING_APPLICATION_JSON` 中的 JSON 会转换成属性源。当前 `JsonParserFactory` 按 Jackson 3、Gson、内置 Basic 解析器的顺序选择实现，没有旧文档所写的 YAML JSON 解析器分支。

JSON 属性源插入位置与环境中已有的属性源有关，不能一概写成优先级最高；例如命令行属性可以覆盖它。

`${random.int(11,22)}` 表示下界包含、上界不包含的随机整数；这些随机属性不适合作为需要跨进程稳定的业务配置。

### MCN 补充配置

`McnEnvironmentPostProcessor` 运行在标准 Config Data 之后，通过 `addLast` 追加 `mcn-global-unique`、`mcn-map` 和 `mcn-default`。这与 Spring Boot 自身的外部配置优先级是两层机制，具体文件与 profile 顺序见[扩展模块](../function/extension.md)。

完整规则参见 [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/reference/features/external-config.html)。

## 日志系统

`LoggingApplicationListener` 在多个阶段参与日志初始化和清理。当前实际使用的日志系统由依赖和配置决定，常见实现包括 Logback、Log4j2 与 Java Logging。

| 阶段 | 主要职责 |
| --- | --- |
| `ApplicationStartingEvent` | 获取日志系统并执行早期初始化准备 |
| `ApplicationEnvironmentPreparedEvent` | 读取日志属性、配置文件和日志级别，初始化日志系统 |
| `ApplicationPreparedEvent` | 将日志系统、日志文件或日志组等相关对象加入可用的容器 |
| `ContextClosedEvent` | 按上下文归属清理相关日志资源 |
| `ApplicationFailedEvent` | 执行失败阶段清理 |

`logging.config` 可以指定日志配置文件。需要 Spring 扩展时使用对应的 `logback-spring.xml` 或 `log4j2-spring.xml`，不要套用旧文档中一个固定的 Groovy / XML 文件扫描序列。

同时设置 `logging.file.name` 和 `logging.file.path` 不会自动拼接成路径；`name` 优先。MCN 的默认文件名与滚动配置见[默认属性源](../function/extension.md#mcn-default)。

## 加载Starters

starter 首先是一个 Maven 依赖入口，不是由类名数量决定是否生效的运行时插件。启用 `@SpringBootApplication` / `@EnableAutoConfiguration` 后，自动配置导入大致经过：

1. 从各依赖的 `AutoConfiguration.imports` 收集候选类并去重。
2. 应用注解的 `exclude` / `excludeName` 与 `spring.autoconfigure.exclude` 排除项。
3. 由 [AutoConfigurationImportFilter](./core.md#autoconfigurationimportfilter) 等机制初步过滤。
4. 按自动配置顺序和前后依赖排序。
5. 解析配置类，并继续依据属性、Bean 定义和应用类型等条件决定是否注册 Bean。

配置顺序可用 `@AutoConfiguration(before / after / beforeName / afterName)`、`@AutoConfigureBefore`、`@AutoConfigureAfter` 和 `@AutoConfigureOrder`。普通 `@Order` / `Ordered` 不是自动配置类排序的替代方式；配置类顺序也不等于最终 Bean 的实例化顺序。

### MCN 的数据源过滤

`OnMissingPrimaryDataSource` 在缺少 `spring.datasource.url` 时过滤 Boot 的三个数据源 / JPA 自动配置类。配置了 MCN 多数据源时，还需要对应模式的依赖与包结构；详见[多数据源配置](../function/autoconfig.md#多数据源配置)。

### 两种元数据处理器

| 处理器 | 主要产物与用途 |
| --- | --- |
| `spring-boot-autoconfigure-processor` | `spring-autoconfigure-metadata.properties`，为自动配置的快速条件判断提供元数据 |
| `spring-boot-configuration-processor` | `spring-configuration-metadata.json`，为 IDE 等工具提供属性名、类型、说明和默认值信息 |

IDE 能补全某个属性，不代表对应自动配置已经启用。反过来，配置类字段的初始值也不总等于开关缺省时的行为，还要检查 `@ConditionalOnProperty(matchIfMissing=...)`。

参考：[Spring Boot 自动配置说明](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)。
