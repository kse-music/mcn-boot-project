---
description: 认识 MCN Boot 的设计约定、核心模块与文档阅读路线。
---

# 简介

把通用配置留给工具，把创造留给开发者。

MCN Boot 是基于 Spring Boot 与 Spring Cloud 的 Java 应用开发工具包。它将可复用的基础设施与约定配置组织在一起，为 Servlet、Reactive 和非 Web 应用提供一致的开发起点。

## 从约定开始

每个应用都会遇到响应模型、异常处理、JSON、参数校验、数据访问和服务调用。MCN Boot 复用 Spring 的开发方式，让这些通用能力按条件衔接，减少项目之间反复编写的基础代码。

- **约定配置**：统一常见的基础行为，让应用有清晰的默认起点。
- **按需组合**：根据应用实际需要选择依赖，由 classpath 和应用属性决定相关能力是否启用。
- **保持熟悉**：沿用 Spring Boot 与 Spring Cloud 的配置和扩展机制。

::: tip 按需启用
可选能力仅在对应依赖存在时激活。引入工具包后，仍需为应用选择实际需要的 Spring Boot starter 和其他组件。
:::

## 模块一览

| 模块 | 主要职责 |
| --- | --- |
| `mcn-boot-dependencies` | 管理 Spring Boot、Spring Cloud 与第三方依赖版本 |
| `mcn-boot-parent` | MCN 模块的共享 Maven 构建配置 |
| `mcn-boot-starter-parent` | 应用接入时使用的依赖与插件管理 |
| `mcn-boot-core` | 共享模型、Jackson 支持、工具与客户端抽象 |
| `mcn-boot-autoconfigure` | 通用 Spring Boot 自动配置 |
| `mcn-cloud-autoconfigure` | Spring Cloud、客户端与安全相关自动配置 |
| `mcn-spring-boot-starter` | 应用接入 MCN Boot 的主要入口 |
| `mcn-swagger3` | springdoc-openapi 与 Knife4j 集成 |

## 阅读路线

1. [环境准备](./ready.md)：检查 Java、Maven 与开发工具。
2. [快速开始](../guide/quick-start.md)：创建并运行第一个应用。
3. [依赖管理](../guide/dependency.md)：选择 Parent POM 或 BOM 的接入方式。
4. [自动配置](../function/autoconfig.md)：了解具体功能与配置说明。
5. [理解 Spring Boot](../spring/core.md)：继续阅读底层机制与源码分析。
