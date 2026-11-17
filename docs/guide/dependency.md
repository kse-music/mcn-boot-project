---
description: 通过 Parent POM 或 BOM 管理依赖版本，按应用需要组合 MCN Boot 组件。
---

# 依赖管理

统一依赖版本，让组件选择保持自由。

## 选择接入方式

| 接入方式 | 适用情况 | 提供的能力 |
| --- | --- | --- |
| Parent POM | 可以继承 `mcn-boot-starter-parent` | 依赖与插件管理 |
| BOM | 需要保留项目现有父 POM | 依赖版本管理 |

使用父 POM 的完整示例见[快速开始](./quick-start.md)。

## 导入 BOM

如果项目已有父 POM，在 `dependencyManagement` 中导入 `mcn-boot-dependencies`，再按需声明 starter 与组件依赖。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.hiboot.mcn</groupId>
            <artifactId>mcn-boot-dependencies</artifactId>
            <version>4.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

::: tip 插件管理
BOM 管理依赖版本。使用 BOM 时，请自行保留并配置应用需要的 Maven 插件。
:::

## 版本属性

下表提供了可用于覆盖 MCN 管理的版本的所有属性。应用可自定义这些版本。

| 库                                    | 版本属性                         |       当前版本       |
|--------------------------------------|:-----------------------------|:----------------:|
| bcpkix-jdk18on                       | bcprov-jdk18on.version       |       1.85       |
| bcprov-jdk18on                       | bcprov-jdk18on.version       |       1.85       |
| spring-boot-admin-starter-client     | boot-admin.version           |      4.1.2       |
| spring-boot-admin-starter-server     | boot-admin.version           |      4.1.2       |
| commons-io                           | commons-io.version           |      2.21.0      |
| guava                                | guava.version                |    33.7.2-jre    |
| hutool-bom                           | hutool.version               |      5.8.41      |
| knife4j-openapi3-ui                  | knife4j.version              |      4.5.0       |
| mapstruct                            | mapstruct.version            |   1.5.5.Final    |
| mybatis-spring-boot-starter          | mybatis-spring-boot.version  |      4.0.1       |
| minio                                | minio.version                |      8.6.0       |
| spring-boot-dependencies             | spring-boot.version          |      4.1.0       |
| spring-cloud-dependencies            | spring-cloud.version         |     2025.1.2     |
| spring-cloud-alibaba-dependencies    | spring-cloud-alibaba.version |    2025.1.0.0    |
| spring-cloud-huawei-bom              | spring-cloud-huawei.version  | 1.11.15-2025.1.x |
| springdoc-openapi-starter-webmvc-ui  | springdoc.version            |      3.0.3       |
| springdoc-openapi-starter-webflux-ui | springdoc.version            |      3.0.3       |
| xxl-job-core                         | xxl-job.version              |      3.2.0       |
