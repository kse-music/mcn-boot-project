---
description: 从一份 Maven 配置开始，构建并运行第一个 MCN Boot 应用。
---

# 快速开始

从一份 Maven 配置开始，构建你的第一个 MCN Boot 应用。

::: tip 开始之前
准备 Java 17+ 和 Maven 3.6.3+。还没有准备好？先查看[环境准备](../intro/ready.md)。
:::

## 新建Maven项目

### 引入父模块

在项目的 `pom.xml` 中继承 `mcn-boot-starter-parent`，复用统一的依赖与插件管理。

```xml
<parent>
    <groupId>cn.hiboot.mcn</groupId>
    <artifactId>mcn-boot-starter-parent</artifactId>
    <version>4.1.0</version>
</parent>
```

::: tip 已有父 POM？
如果项目需要保留现有父 POM，可以通过[导入 BOM](./dependency.md#导入-bom)管理依赖版本。
:::

### 添加应用依赖

添加 MCN Boot starter，再选择应用所需的 Spring Boot starter。下面以 Servlet 应用为例。

```xml
<dependencies>
    <dependency>
        <groupId>cn.hiboot.mcn</groupId>
        <artifactId>mcn-spring-boot-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
</dependencies>
```

可选能力在对应依赖存在时按条件启用，只引入应用实际需要的组件。

## 组织包结构

在 `src/main/java/com/example/demo/` 下创建应用入口。控制器放在同一包或其子包中，便于 Spring Boot 扫描。

### 创建启动类

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 添加一个接口

```java
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, MCN Boot!";
    }
}
```

在 `src/main/resources/application.yml` 中设置应用名称：

```yaml
spring:
  application:
    name: demo
```

## 运行&访问

在应用根目录运行：

```sh
mvn spring-boot:run
```

也可以在 IDE 中运行 `DemoApplication`。启动后访问 `http://localhost:8080/hello`，确认接口返回 `Hello, MCN Boot!`。

::: tip 本地安装
如果当前源码版本尚未发布到 Maven 仓库，先在 MCN Boot 项目根目录执行 `mvn install`，然后启动你的应用。
:::

## 添加 API 文档

需要接口文档时，按需添加 `mcn-swagger3`：

```xml
<dependency>
    <groupId>cn.hiboot.mcn</groupId>
    <artifactId>mcn-swagger3</artifactId>
</dependency>
```

在应用配置中开启 Swagger：

```yaml
swagger:
  enabled: true
  title: ${spring.application.name} API
  version: 1.0.0
```

重新启动应用后，访问 `http://localhost:8080/doc.html`。

接下来可以阅读[依赖管理](./dependency.md)和[自动配置](../function/autoconfig.md)，按业务需要继续组合组件。
