---
description: MCN 属性源的加载顺序、默认配置、日志与引导上下文行为。
---

# 扩展模块

`McnEnvironmentPostProcessor` 在 Spring Boot 的 `ConfigDataEnvironmentPostProcessor` 之后执行，顺序为 `ConfigDataEnvironmentPostProcessor.ORDER + 1`。它通过 `addLast` 将 MCN 属性源加入环境，作为可被先前应用配置覆盖的默认值。

## mcn-global-unique

读取应用提供的 `classpath:config/mcn.properties`。如果已经激活 profile，还会尝试读取 `classpath:config/mcn-{profile}.properties`。

对于 `spring.profiles.active=dev,local`，MCN 从后往前加入 profile 属性源，优先级为：

```text
已有的高优先级应用属性源
  > mcn-global-unique-local
  > mcn-global-unique-dev
  > mcn-global-unique
  > 后续加入的 mcn-map 与 mcn-default
```

文件不存在时跳过对应属性源。基础文件已经以 `mcn-global-unique` 加载后，不再重复加载；不要依赖运行中修改 profile 来重新触发这一流程。

这些文件是 MCN 的补充配置，不会替代 Spring Boot 的 `application.properties`、`application.yml`、环境变量和命令行参数机制。

## mcn-map

保存启动时计算得到的值：

| 属性 | 来源与边界 |
| --- | --- |
| `app.base-package` | 能识别主应用类时，取该类所在包 |
| `logging.level.<应用包>.dao` | 能识别主应用类时，默认 `info` |
| `project.version` | 主应用类的版本信息；无法读取时不加入 |
| `mcn.log.file.name` | 使用已有环境值，缺失时为 `error` |
| `mcn.version` | `v` 加 MCN 类的版本信息；IDE 中运行时可能没有有效的 Manifest 版本 |

依赖应用包推导的功能，例如多数据源包扫描，不应假设所有嵌入式启动或测试环境都存在主应用类。需要时显式配置 `base.package.name`。

## mcn-default

加载框架内置的 `mcn-default.properties`，并将 classpath 中 `config/config.properties` 的配置合并进该属性源。同名属性以合并后的值为准。

部分实际默认配置如下：

| 配置 | 默认值或行为 |
| --- | --- |
| `logging.file.name` | `${logging.baseDir:/work/logs}/${spring.application.name:none}/${mcn.log.file.name}.log` |
| `logging.level.root` | `info` |
| `logging.logback.rollingpolicy.max-history` | `30` |
| `logging.logback.rollingpolicy.max-file-size` | `100MB` |
| `server.tomcat.basedir` | `/tmp/tomcat/`，非 Unix 部署应按环境覆盖 |
| `server.compression.enabled` | `true` |
| `mybatis.mapper-locations` | `classpath:mapper/**/*.xml` |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` |
| `spring.sql.init.encoding` | `utf-8` |
| `spring.cloud.openfeign.client.config.default.connect-timeout` | `10000` 毫秒 |
| `spring.cloud.openfeign.client.config.default.read-timeout` | `60000` 毫秒 |

日志滚动和清理规则由实际日志系统及其配置执行，不能据此断言任意自定义日志配置都会清理 30 天前的文件。

[查看完整默认配置](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/config/mcn-default.properties)。

## 引导上下文

检测到名为 `bootstrap` 的属性源时，默认仍尝试加载 `mcn-global-unique`，随后跳过 `mcn-map` 和 `mcn-default`。需要提前加载后两者时使用：

```properties
mcn.bootstrap.eagerLoad.enabled=true
```

该设置不会自行创建 Spring Cloud 引导上下文。SM4 配置解密等需要 bootstrap 的功能，还要满足相应依赖和启动条件。

如果环境已经存在 `mcn-default`，`loadDefaultConfig` 会直接返回，不再添加 `mcn-map` 或重复加载默认配置。

## 属性源打印

```properties
mcn.print-env.enabled=false
```

默认关闭。开启后，`McnApplicationListener` 在 `ApplicationContextInitializedEvent` 遍历可枚举属性源，并逐项记录原始属性值；当前实现没有脱敏。不要在含有真实密码、令牌或其他敏感配置的环境中开启。

## 源码位置

- [McnEnvironmentPostProcessor](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/context/McnEnvironmentPostProcessor.java)
- [McnApplicationListener](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/context/McnApplicationListener.java)

Spring Boot 自身的配置优先级见[配置解析](../spring/source-parse.md#配置解析)。
