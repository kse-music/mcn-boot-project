---
description: 当前教程对应的项目源码、Spring Boot 官方参考与示例验证方式。
---

# 参考资料

## 项目源码

- [MCN Boot 项目](https://github.com/kse-music/mcn-boot-project)
- [通用自动配置清单](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)
- [Cloud 自动配置清单](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-cloud-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)
- [默认属性配置](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/config/mcn-default.properties)

## Spring Boot 参考

当前仓库使用 Spring Boot 4.1.0。官方站点的默认版本可能继续升级；分析类名、调用链和条件装配时，应对照依赖版本一致的 `*-sources.jar`。

- [应用启动与事件](https://docs.spring.io/spring-boot/reference/features/spring-application.html)
- [外部化配置](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [自动配置](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)
- [编写自动配置](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
- [传统 WAR 部署](https://docs.spring.io/spring-boot/how-to/deployment/traditional-deployment.html)

## 示例验证

关键 Java 和 properties 示例由 `docs/examples/` 维护，文档直接引用这些文件。在仓库根目录执行：

```sh
mvn -Pdocs-check test
npm run docs:build
```

`docs-check` 仅在显式启用时，将示例模块加入 reactor。测试覆盖响应 JSON、配置绑定、正则规则、数据源上下文恢复、锁释放、SM2 往返与摘要计算等行为；不连接真实数据库、Redis、配置中心或外部 Servlet 容器。

应用接入时仍需验证自己的依赖组合、网络服务和部署环境。
