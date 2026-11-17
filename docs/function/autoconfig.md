---
description: 当前 MCN 自动配置的启用条件、配置默认值、可编译示例与使用边界。
---

# 自动配置

本页对应当前仓库的 MCN Boot 4.1.0。先按[快速开始](../guide/quick-start.md)接入 MCN；可选功能还需要应用显式引入对应依赖。配置属性类中的字段初始值与自动配置缺省时是否启用是两回事，以下按实际条件说明。

## 统一异常处理

在 Web 应用中，`ExceptionHandlerAutoConfiguration` 注册默认异常处理器，再由 MVC / WebFlux 集成将异常转换为 `RestResp`。业务异常使用 `ServiceException.newInstance(code, message)`，或自定义继承 `BaseException` 的异常。

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/ErrorConfiguration.java

这个配置中的解析器只接管原因链中错误码为 `SPECIAL_SYMBOL_ERROR` 的 `ServiceException`；其余异常返回 `null`，交给后续解析流程。自定义解析器不要把不负责的异常伪装成成功响应。

### 错误码与消息

- `BaseException` 的默认业务错误码为 `999999`。
- 没有特殊匹配的普通异常默认错误码为 `999998`；参数绑定、JSON、上传、HTTP 状态等异常另有专门映射。
- `RestResp` 错误码不是 HTTP 响应状态码。HTTP 状态还受具体 MVC / WebFlux 处理边界影响。
- `ErrorMsg` 先读取内置 `mcn-error-msg.properties`，再读取应用 classpath 根目录的 `error-msg.properties`；未找到的消息是空字符串，不能保证自动回退为某条“内部错误”文本。

`mcn.exception.handler` 的关键配置：

| 属性 | 默认值 | 含义 |
| --- | --- | --- |
| `return-origin-ex-msg` | `true` | 返回原始异常消息；对外服务可显式设为 `false`，并配置合适的错误消息 |
| `return-validate-result` | `false` | 是否在响应中返回校验明细 |
| `validate-result-to-error-info` | `true` | 把首条校验消息放入 `ErrorInfo` |
| `append-field` | `false` | 校验消息是否附带字段名 |
| `override-ex-msg` | `false` | 是否按 `error-code-msg` 覆盖响应消息 |
| `log-ex-msg` | `true` | 是否记录异常栈 |
| `log-no-resource` | `false` | 是否记录未找到资源的异常栈 |
| `remove-framework-stack` | `true` | 默认处理器中的框架栈裁剪开关 |

```properties
mcn.exception.handler.return-origin-ex-msg=false
mcn.exception.handler.override-ex-msg=true
mcn.exception.handler.error-code-msg[400001]=名称不能为空
```

## Swagger配置

按需引入 `cn.hiboot.mcn:mcn-swagger3`，当前集成为 springdoc-openapi 3 与 Knife4j OpenAPI 3 UI。

```xml
<dependency>
    <groupId>cn.hiboot.mcn</groupId>
    <artifactId>mcn-swagger3</artifactId>
</dependency>
```

```properties
swagger.enabled=true
swagger.title=Demo API
swagger.version=1.0.0
```

默认不启用 MCN 的 Swagger 配置。MCN 初始化器还会在开关关闭时，提供低优先级的 `springdoc.api-docs.enabled=false` 与 `springdoc.swagger-ui.enabled=false`；应用显式设置的高优先级属性仍可覆盖它们。

- 使用 OpenAPI 3 的 `@Operation`、`@Tag`、`@Schema`、`@Hidden`，包名为 `io.swagger.v3.oas.annotations...`。
- MCN 的 `cn.hiboot.mcn.swagger.IgnoreApi` 标注了 `@Hidden`，可用于类或方法；旧的 `io.swagger.annotations.Api`、`ApiOperation`、`ApiIgnore` 不属于当前教程的接入方式。
- 默认入口为 `/doc.html`，OpenAPI JSON 路径为 `/v3/api-docs`；最终可达性还受 Springdoc、上下文路径与安全配置影响。
- `swagger.header.authorization` 缺省为 `null`，此时根据 Spring Security 的 `Authentication` 类是否存在决定是否添加 Bearer scheme。它只描述文档，不创建认证策略。
- `swagger.header.csrf` 默认为 `false`；`swagger.server.url`、`swagger.server.description` 可覆盖文档中的服务地址。

需要纯 Reactive 应用时，同时核对 Springdoc WebFlux 依赖与应用类型，不能只把 MVC starter 改名后认为整套依赖自动完成切换。

下面是使用当前注解的可编译接口示例：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/OpenApiExample.java

## 跨域配置

开关是 `filter.cross.enabled=true`，默认关闭。规则绑定在 `mcn.cors` 下，支持 Servlet 与 Reactive 应用。

<<< @/examples/src/main/resources/cors.properties

| 属性 | 默认值 / 说明 |
| --- | --- |
| `pattern` | `/**` |
| `allow-credentials` | `null`，未显式允许携带凭据 |
| `allowed-origin` / `allowed-header` / `allowed-method` | 单个值，默认均为 `*` |
| `allowed-origins` / `allowed-headers` / `allowed-methods` | 列表值；配置后通过对应 setter 覆盖单值构造的列表 |
| `allowed-origin-pattern` / `allowed-origin-patterns` | 配置后优先走来源模式分支，不再使用普通来源分支 |
| `exposed-header` / `exposed-headers` | 暴露给浏览器的响应头，默认未设置 |
| `max-age` | `3600` 秒 |
| `order` | `-1000`，用于 Servlet 过滤器注册；Reactive 分支没有使用这个属性设置顺序 |

多个来源、方法、请求头使用复数属性。单值 `allowed-method=GET,POST` 会被当成一次 `addAllowedMethod` 的输入，不等同于正确的两个方法。

生产环境是否开启 CORS 由前后端的实际跨域需求决定。携带 Cookie 等凭据时，配置明确可信来源，不能把普通 `allowed-origin=*` 与 `allow-credentials=true` 组合使用。CORS 也不是服务端身份认证或权限控制。

## XSS配置

`mcn.xss.enabled=true` 时启用对应 Web 过滤器。默认处理器调用 `HtmlUtils.htmlEscape`，属于 HTML 字符转义，并不执行完整的富文本净化或所有上下文的 XSS 防护。

```properties
mcn.xss.enabled=true
mcn.xss.include-urls=/api/**
mcn.xss.exclude-urls=/api/health
mcn.xss.process-payload=false
mcn.xss.failed-fast=false
```

| 属性 | 默认值 | 行为 |
| --- | --- | --- |
| `include-urls` | `/**` | 处理范围，Ant 路径模式 |
| `exclude-urls` | 空列表 | 排除范围，优先于包含规则 |
| `exclude-fields` | 未设置 | 不处理的请求字段名；共享响应字符串序列化器不传字段名，不能据此排除响应字段 |
| `process-payload` | `false` | 是否由请求包装器处理整段原始正文；不控制 Jackson 的字符串反序列化路径 |
| `filter-parameter-name` | `false` | 是否处理键值参数的名称 |
| `filter-header-value` | `false` | 是否处理请求头的值 |
| `failed-fast` | `false` | 转义会改变输入时，是抛出异常还是返回转义后的文本 |
| `escape-response` | `false` | 是否在共享 Jackson 字符串序列化路径中调用 XSS 处理器，不是所有响应类型的统一改写 |
| `order` | `Integer.MAX_VALUE` | 默认排在较后的位置 |

Web 应用存在 `NameValueProcessor` Bean 时，`FilterAutoConfiguration` 还会安装 Jackson 3 字符串序列化 / 反序列化自定义器。使用该映射器读取 JSON 字符串字段时，即使 `process-payload=false` 仍会调用处理器。例如 `<b>` 会变为 `&lt;b&gt;`。响应字符串默认不转义，只有 XSS 的 `escape-response=true` 才处理这一序列化路径；再次转义已转义的文本可能产生双重转义。

对于 JSON 接口，示例保留 `process-payload=false`。当前 Servlet 包装器在该项为 `true` 时会把整段正文交给 `HtmlUtils.htmlEscape`，连 JSON 的结构引号也可能转义，导致后续 JSON 解析失败；不要把它当作按字段处理 JSON 的开关。

这些请求匹配器还会合并启用的 `web.security.default-exclude-urls`，详见下一节。共享 Jackson 路径通过当前 Servlet 请求匹配 URL；没有 Servlet 请求上下文时直接处理，因此不能假定 Reactive 或后台序列化同样受 URL 排除规则保护。MCN 显式标记的 Feign 请求会跳过这一共享 Jackson 处理路径。

## WebSecurity配置

在 Spring Security 的 `WebSecurity` 类存在时，MCN 注册相关配置。Servlet 使用 `WebSecurityCustomizer` 和 `PathPatternRequestMatcher`；Reactive 分支以独立过滤器处理忽略路径。

MCN 默认属性源中的忽略路径是：

```properties
web.security.default-exclude-urls=/v3/api-docs/**,/swagger-ui/**,/doc/**,/doc.html,/webjars/**,/error,/favicon.ico,/_imagePreview,/_groovyDebug_
```

`enable-default-ignore` 默认 `true`；应用可覆盖默认列表，也可添加 `exclude-urls`。例如只为明确的静态资源额外放行：

```properties
web.security.enable-default-ignore=false
web.security.exclude-urls=/assets/**
```

::: warning 忽略路径的实际含义
Servlet 的 `web.ignoring()` 会跳过安全过滤器链，不等同于授权规则中的 `permitAll()`；Reactive 的忽略分支也绕过后续链并直接调用 handler。不要把业务接口随意加入忽略列表。应用仍需自行配置认证与授权策略。
:::

XSS、参数预处理、完整性校验等 MCN 请求匹配器使用 Ant 路径模式，且会在默认忽略开关开启时合并默认排除列表；这个合并不会自动加入应用的 `web.security.exclude-urls`。Spring Security 自身的路径匹配规则与这些过滤器的规则应分别核对。

## Validator配置

引入 `org.springframework.boot:spring-boot-starter-validation`。当前注解来自 `jakarta.validation`，配置前缀为 `mcn.validator`。

```properties
mcn.validator.fail-fast=true
```

默认 `fail-fast=true`；允许覆盖方法参数约束、多个返回值级联校验、平行方法约束的三个附加开关均为 `false`。自定义器只在实际校验配置为 Hibernate Validator 的 `ConfigurationImpl` 时应用这些设置，不能保证对任意校验提供者生效。

`cn.hiboot.mcn.autoconfigure.validator.annotation.Phone` 允许空值或空字符串，非空输入仅检查 `^\d{11}$`，不会校验真实号码归属。必填场景还需组合 `@NotBlank`。

`ValidGroup` 继承 Jakarta Validation 的 `Default`；使用不继承 `Default` 的自定义组时，如需同时校验默认组，应显式把默认组加入校验范围。

## 多数据源配置

需要 HikariCP 与对应数据访问依赖。最简单的动态路由场景可以引入 `spring-boot-starter-jdbc`、相应 JDBC 驱动，以及用于启用 AOP 的 `spring-boot-starter-aspectj`。

```properties
multiple.datasource.hello.url=${HELLO_DB_URL}
multiple.datasource.hello.username=${HELLO_DB_USER}
multiple.datasource.hello.password=${HELLO_DB_PASSWORD}
multiple.datasource.world.url=${WORLD_DB_URL}
multiple.datasource.world.username=${WORLD_DB_USER}
multiple.datasource.world.password=${WORLD_DB_PASSWORD}
dynamic.datasource.enabled=true
```

数据源名称为 `hello` 与 `world`。MCN 绑定这些 `DataSourceProperties` 后，创建 `helloDataSource`、`worldDataSource` 等 Hikari 数据源。

### 模式选择

| 模式 | 开关 | 主要行为 |
| --- | --- | --- |
| 动态路由 | `dynamic.datasource.enabled=true` | 注册 primary 路由数据源与 `SwitchSourceAdvisor` |
| MyBatis 独立多源 | `mybatis.multiple.datasource.enabled=true` | 为每个源注册 SqlSessionFactory、SqlSessionTemplate 并扫描各自 Mapper |
| JPA 独立多源 | `jpa.multiple.datasource.enabled=true` | 为每个源注册 EntityManagerFactory、事务管理器和 Repository |

三个开关同时启用超过一个会抛出异常。都未启用时，只有 `dynamic.datasource.enabled` 未设置或为 `true` 才走动态模式；显式设置为 `false` 会关闭这一默认回退。

动态模式有普通 `dataSource` Bean 时将其作为默认目标，否则取绑定 Map 遍历的首个数据源。当前没有额外的“按名称指定 primary”属性，调用方应明确设计默认源与路由边界。

### 在MyBatis和Jpa使用动态数据源

分别引入 `org.mybatis.spring.boot:mybatis-spring-boot-starter` 或 `org.springframework.boot:spring-boot-starter-data-jpa`。`@SwitchSource` 可以标注类或方法，当前切面取方法注解优先于声明类注解。

下面同时展示代理调用方式与一个显式管理上下文的示例方法：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/RoutingExamples.java

::: warning 路由与事务边界
注解依赖 Spring AOP 代理，类内直接调用不会自动触发切面。当前 `SwitchSourceAdvisor` 只在正常返回后清理标识，没有在 `finally` 中清理，也不会恢复外层标识；异常或嵌套路由不能假定安全恢复。示例的 `withDataSource` 显式使用 `try/finally` 恢复原标识。路由应在获取数据库连接、开启相应事务之前确定，已建立的事务连接不会因为后续切换标识而自动换库。
:::

### MyBatis和Jpa使用多数据源

独立多源的扫描前缀来自 `base.package.name`，缺失时取 `app.base-package`；`dao.package.name` 默认为 `dao`。

| 项目 | 当前扫描 / 注册约定 |
| --- | --- |
| MyBatis Mapper | `<基础包>.<dao包>.<源名>` |
| MyBatis XML | `classpath:mapper/<源名>/*.xml`，不是递归 `**/*.xml` |
| MyBatis 类型别名 | `<基础包>.bean.<源名>` |
| MyBatis 类型处理器 | `<基础包>.<dao包>.handler.<源名>` |
| JPA Repository | `<基础包>.<dao包>.<源名>` |
| JPA Entity | `<基础包>.bean`，当前实现并未按源名隔离实体包 |
| JPA 事务管理器 | `<源名>TransactionManager` |

选择独立多源后，不使用 `@SwitchSource` 来替代各 Mapper / Repository 已绑定的数据源。需要事务时，明确指定对应的事务管理器，并核对应用是否另外配置了普通主数据源。

JPA 独立多源还依赖 `EntityManagerFactoryBuilder`。当前 `OnMissingPrimaryDataSource` 会在缺失普通 `spring.datasource.url` 时排除 Boot 的 Hibernate 自动配置，因此仅配置命名数据源与 JPA 多源开关并不足够：需要保留普通主数据源配置，或由应用显式提供相应的 JPA 基础设施。

## 传输加解密

### 使用SM2加密配置

需要 `cn.hutool:hutool-crypto` 和 `org.bouncycastle:bcprov-jdk18on`。MCN starter 已包含 Cloud 自动配置模块；单独接入时还需 `mcn-cloud-autoconfigure`。

```properties
encryptor.sm2.private-key=${MCN_SM2_PRIVATE_KEY}
encryptor.sm2.public-key=${MCN_SM2_PUBLIC_KEY}
encryptor.sm2.mode=C1C3C2
encryptor.continue-on-error=false
```

两个密钥属性必须存在且可解析为有效密钥。不要复制共享示例密钥；通过受保护的环境或配置来源注入自己的密钥。默认输出 Hex，`base64`、`bcd`、`lower-case` 均默认 `false`，模式默认 `C1C3C2`。

SM2 的接口是 **`cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor`**：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/EncryptionExample.java

启用后，MVC / WebFlux 为带 `@Decrypt` 的裸 `String` 参数增加解析器，按编译保留的参数名读取对应查询参数；应用需保留 `-parameters` 编译选项。MVC 还支持在 `@RequestBody` 参数上标注 `@Decrypt` 来解密整段正文，之后再交给消息转换器。

Jackson 3 字段解密由 `DecryptJacksonAutoConfiguration` 安装自定义注解内省器。当前字段反序列化路径按 setter 参数类型转换，不能把任意 record、构造器绑定或所有字段形态都视为同等支持。

::: warning 解密失败的不同路径
直接调用 `SM2Encryptor.decrypt` 时，`encryptor.continue-on-error=false` 会传播解密异常；设为 `true` 时可能返回原文。Jackson 的 `DecryptDataSerializer` 默认路径另外捕获了解密异常并继续转换原值，因此字符串字段可能保留输入。`@Decrypt` 不能作为“必定拒绝明文或非法密文”的校验保证，应按实际接入路径另做输入验证。
:::

## 完整性校验

### 使用SM3计算摘要

需要 Hutool Crypto 与 Bouncy Castle，使用 `data.integrity.enabled=true` 开启 Servlet 或 Reactive 分支。

```properties
data.integrity.enabled=true
data.integrity.include-patterns=/api/**
data.integrity.exclude-patterns=/api/health
data.integrity.check-upload=false
data.integrity.check-replay=false
data.integrity.timeout=1m
```

| 属性 | 默认值 / 含义 |
| --- | --- |
| `include-patterns` | `/**`，Ant 路径模式 |
| `exclude-patterns` | 空列表，还会合并启用的默认排除路径 |
| `order` | `-1000` |
| `check-upload` | `false`，是否将上传文件信息纳入摘要 |
| `check-replay` | `false`，是否检查时间戳已过期 |
| `timeout` | `1m`，时间戳过期窗口 |

请求头为 `TSM`（毫秒时间戳，也兼容 `timestamp`）、`nonceStr`、`signature`。Java 调用方可以复用当前算法生成这些值：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/IntegrityExample.java

算法将参数名排序，跳过名为 `signature` 的参数，使用 `key=value` 和 `&` 拼接；随后拼接可选的文件信息与原始 JSON 正文，再加 `timestamp`、`nonceStr` 后计算 SM3。JSON 的空格等原始字节内容会影响结果，不应先重新格式化正文再发送。

当前 Servlet 分支对重复参数取最后一个值；Feign 拦截器对同名查询参数取第一个值，跨端接入不要直接使用重复参数。Feign 的自动添加请求头还需要对应 OpenFeign 集成，并单独开启 `data.integrity.interceptor.enabled=true`，默认不启用。

::: warning 安全边界与实现限制
这里是没有秘密密钥参与的摘要，不是身份认证、HMAC 或数字签名。`check-replay` 当前只检查旧时间戳，没有 nonce 去重，也没有拒绝未来时间戳的对称窗口。Reactive JSON 分支只对代码中的 POST + JSON 路径按数据块处理，不能保证任意分块正文、PUT / PATCH 或 multipart 与 Servlet 行为一致。校验失败的当前诊断日志会包含参数、正文等原始内容；涉及敏感载荷的接入必须先处理这一日志边界。
:::

## 配置加解密

### 使用SM4加密配置

SM4 用于 Spring Cloud 的配置解密。需要 `spring-cloud-starter-bootstrap`、`spring-security-crypto`、Hutool Crypto 与 Bouncy Castle。它通过 `spring.factories` 的 Cloud bootstrap 配置注册，不在普通 Cloud 自动配置清单中。

在 bootstrap 阶段可用的配置源中提供属性，例如应用的 `bootstrap.properties`：

```properties
encryptor.sm4.key=${MCN_SM4_KEY}
encryptor.sm4.iv=${MCN_SM4_IV}
encryptor.sm4.mode=CBC
encryptor.sm4.padding=PKCS5Padding
encryptor.continue-on-error=false
```

当前实现按 UTF-8 取 `key` / `iv` 的字节，SM4 密钥需要 16 字节；使用 CBC 时 IV 也需要 16 字节。`mode` 和 `padding` 都显式给定才进入相应构造分支，否则走 Hutool 默认构造。密钥不必硬编码在 `config/mcn.properties`，但必须在创建 bootstrap 加密器之前可解析，不能只放在解密之后才加载的配置源中。

受保护配置中的密文用 `{cipher}` 前缀标记，密文应由同一参数集生成。本页不提供可跨环境复用的固定密钥、固定测试密文或对外加解密接口。

这个接口是 **`org.springframework.security.crypto.encrypt.TextEncryptor`**，与 SM2 的同名接口不同。不要把 SM4 的 Bean 当作 `@Decrypt` 所需的 SM2 Bean 注入。

`encryptor.continue-on-error` 默认 `false`。开启容错后失败可能返回原文，不能当作已解密成功。具体密文保护范围、密钥来源和存储格式仍由应用设计。

## 参数预处理

开关为 `param.processor.enabled=true`。内置 `ParamProcessor` 的正则语义是 **完整匹配则拒绝**，不是匹配则放行。`global.rule.pattern` 缺省为空字符串，表示不执行默认正则拒绝规则。

下面的配置同时演示 XSS 与参数预处理，可按需要仅保留对应前缀。两者同时启用时，默认参数过滤器先于 XSS 过滤器执行。

<<< @/examples/src/main/resources/filters.properties

`(?s).*[<>].*` 会拒绝包含尖括号的文本。旧示例中的 `global.rule.pattern=.*` 会拒绝所有匹配的输入，不应作为“允许任意值”的设置。

| 属性 | 默认 / 缺省行为 |
| --- | --- |
| `use-filter` | 虽然属性字段初值为 `false`，但注册条件 `matchIfMissing=true`，主开关启用且该项未设置时仍注册过滤器；显式设为 `false` 才关闭过滤器路径 |
| `process-payload` | `false`，控制过滤器是否处理整段原始正文，不关闭共享 Jackson 的字符串处理 |
| `include-urls` | `/**` |
| `exclude-urls` | 空列表 |
| `exclude-fields` | 未设置 |
| `filter-parameter-name` / `filter-header-value` | 均为 `false` |
| `order` | `Integer.MAX_VALUE - 1` |

`use-filter=false` 只关闭对应过滤器。主开关开启并注册处理器后，共享 Jackson 反序列化路径仍可按 `global.rule.pattern` 拒绝 JSON 字符串字段；普通参数处理器在共享响应序列化路径中会被跳过。请求 URL 匹配的边界见上面的 XSS 说明。

`@CheckParam` 可标注类型、参数或字段，为指定对象 / 字段提供规则，注解路径不受过滤器 URL 黑白名单的相同限制。规则依旧是匹配即拒绝，不能把 `[a-z]+` 直接当作允许小写字母的规则。

实现自己的 `ParamProcessor` Bean 可以替换默认处理器；方法参数为 `rule`、`name`、`value`，返回处理后的字段值。自定义规则含义需要由该实现明确约定。`@CheckParam` 的对象绑定与 Jackson 路径按当前解析器支持的 Bean 形态工作，不能由注解存在推断任意嵌套模型都已校验。

## 分布式锁

引入 `spring-boot-starter-data-redis`，并配置可用的 `StringRedisTemplate`。`RedisToolAutoConfiguration` 才会注册默认 `DistributedLocker`；注解切面另需 AspectJ，当前 Spring Boot 4 对应依赖为 `spring-boot-starter-aspectj`。

默认等待时间与租约时间均为 5 秒，重载可使用 `TimeUnit` 指定时间单位。

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/LockExamples.java

- 显式锁操作在成功获取后用 `try/finally` 释放，不要把手动锁和注解锁叠加在同一个临界区示例上。
- 默认 `RedisDistributedLocker.tryLock` 等待超时会抛出 `ServiceException`，不能只写一个 `if` 的失败分支就认为超时已处理。
- 接口默认 `tryExecute` 只有在 `tryLock` 返回 `true` 时执行回调；自定义实现返回 `false` 时，Runnable 不执行，Supplier 重载返回 `null`。默认 Redis 实现超时抛异常的行为仍会传播。
- `@DistributedLock` 只有锁名参数，使用默认等待与租约；需要经过 Spring AOP 代理，不能依赖类内自调用。

::: warning 租约限制
当前实现没有自动续租。Redis 锁值使用锁名，未使用每次获取独立的 owner token；超过租约后，旧持有者解锁可能影响新持有者的锁。不要把这套实现描述为租约过期后仍保证互斥的通用锁服务，关键业务需先选定满足其一致性要求的实现。
:::

## 打War包

此方式面向 Servlet 应用，不适用于把 WebFlux 应用直接部署成传统 Servlet WAR。使用兼容当前 Servlet / Spring Boot 版本的外部容器，并单独验证部署。

```xml
<packaging>war</packaging>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

不要仅排除 Tomcat 后就认为运行环境完整；直接声明 `provided` 让构建保留编译需求，同时由外部容器提供运行时。WAR 插件版本沿用项目父 POM 的管理，具体打包可配置 `org.apache.maven.plugins:maven-war-plugin`。

MCN 的 `WarConfiguration` 继承 `SpringBootServletInitializer`，从 `classpath:config/mcn.properties` 读取 `main.class`，因此使用这一入口时应提供：

```properties
main.class=com.example.demo.DemoApplication
```

这里应填写应用真实的启动类。若自行提供另一套 `SpringBootServletInitializer`，需要同时核对 MCN 入口的参与情况，避免创建重复上下文。

参考：[Spring Boot 传统部署](https://docs.spring.io/spring-boot/how-to/deployment/traditional-deployment.html)。

## 数据库初始化

普通初始化仍由 Spring Boot 决定。MCN 的附加初始化还要求存在 `ApplicationDataSourceScriptDatabaseInitializer` Bean；不能仅凭 classpath 中有 SQL 文件就断言一定执行。

<<< @/examples/src/main/resources/sql-init.properties

当前 MCN 附加初始化器按以下阶段执行：

1. `schema-<platform>.sql` 与 `schema.sql`。
2. `other-<platform>.sql` 与 `other.sql`。
3. `data-<platform>.sql` 与 `data.sql`。

默认目录为 `db`，采用 `optional:classpath*:` 位置，可聚合多个资源。以上平台专用和通用文件都会被查找，不是找到一个就停止；同一位置的多个资源按 URL 字符串排序。

例如 `platform=mysql` 时，可以在 `src/main/resources/db/schema-mysql.sql` 放入与目标数据库匹配的建表脚本：

```sql
CREATE TABLE IF NOT EXISTS t_note (
    id BIGINT PRIMARY KEY,
    body VARCHAR(255)
);
```

`schema`、`data` 使用 `spring.sql.init.separator`，`other` 使用 `spring.sql.init.additional.separator`，后者默认 `//`。数据库方言、触发器与驱动需要单独验证，不能把某个 Kingbase 兼容模式下的示例当成所有环境通用 SQL。

| 配置 | 当前行为 |
| --- | --- |
| `spring.sql.init.mode` | 非嵌入式数据库需要按需求显式设置；`never` 禁止脚本执行，`always` 显式允许 |
| `spring.sql.init.continue-on-error` | 示例设为 `false`，让脚本错误暴露出来 |
| `spring.sql.init.additional.enabled` | 缺省时条件允许启用；属性类的字段初值虽为 `false`，注册条件实际为 `matchIfMissing=true`。建议显式配置 |
| `spring.sql.init.additional.dir` | `db` |
| `spring.sql.init.additional.separator` | `//` |
| `spring.sql.init.additional.init-db-name` | 默认未设置；非空时尝试用另一个已存在的库建立连接再创建目标库，需要匹配的 URL 与建库权限 |

附加初始化器与 Boot 原有初始化器可以同时存在。避免在两套位置放入会重复执行的非幂等脚本，也不要把启动初始化当作具备版本迁移管理的数据库迁移工具。默认建议预先创建数据库；当前自动建库失败会记录错误，不能保证建库成功，也不能保证所有 URL 和数据库方言都受支持。

MySQL 的当前驱动坐标为 `com.mysql:mysql-connector-j`；第三方数据库驱动按厂商实际分发坐标接入，不继续沿用旧的 `mysql:mysql-connector-java` 或未验证的 Kingbase 驱动示例。

## 源码与验证

- [通用自动配置源码](https://github.com/kse-music/mcn-boot-project/tree/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure)
- [Cloud 加解密源码](https://github.com/kse-music/mcn-boot-project/tree/master/mcn-cloud-autoconfigure/src/main/java/cn/hiboot/mcn/cloud/encryptor)
- [Swagger 配置源码](https://github.com/kse-music/mcn-boot-project/tree/master/mcn-swagger3/src/main/java/cn/hiboot/mcn/swagger)

关键 Java 与 properties 示例直接引用 `docs/examples/`，可在仓库根目录执行 `mvn -Pdocs-check test`。测试覆盖配置绑定与示例行为；真实数据库、Redis、Feign 网络调用和外部容器部署仍需由应用做环境验证。
