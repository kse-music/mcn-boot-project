---
description: 准备 MCN Boot 应用所需的 Java 17、Maven 与开发环境。
---

# 环境准备

开始之前，准备一个可用的 Java 开发环境。

## 开发工具

| 工具 | 要求与用途 |
| --- | --- |
| JDK | Java 17 或更高版本 |
| Maven | 3.6.3 或更高版本，管理依赖与构建应用 |
| IDE | 使用熟悉的 Java IDE，例如 IntelliJ IDEA |

在终端检查工具是否可用：

```sh
java -version
mvn -version
```

确认 Maven 输出中的 Java 版本也满足要求。IDE 与终端可能使用不同的 JDK，需要分别检查。

## 技术基线

当前仓库的技术基线为：

| 组件 | 版本 |
| --- | --- |
| MCN Boot | 4.1.0 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Spring Cloud Alibaba | 2025.1.0.0 |

::: tip 使用源码版本
如果当前源码版本尚未发布到 Maven 仓库，请先在 MCN Boot 项目根目录执行 `mvn install`，将所需模块安装到本地仓库。
:::

环境准备好后，继续阅读[快速开始](../guide/quick-start.md)。
