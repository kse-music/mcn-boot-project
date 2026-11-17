---
description: 当前 RestResp 响应契约、Jackson 3 工具与 Spring 上下文访问方式。
---

# 通用模块

本页对应当前仓库的 `mcn-boot-core`。JSON 数据绑定使用 Jackson 3，注解仍来自 `com.fasterxml.jackson.annotation`。

## 统一数据结构

`cn.hiboot.mcn.core.model.result.RestResp<T>` 提供成功响应、分页计数和错误响应，不会自动包装控制器返回的所有对象。需要这个响应契约时，显式返回 `RestResp<T>`。

| JSON 字段 | 类型与默认行为 |
| --- | --- |
| `ActionStatus` | `OK` 或 `FAIL`；普通构造器默认 `OK` |
| `ErrorCode` | 成功默认为 `0` |
| `ErrorInfo` | 成功默认为空字符串；`NON_NULL` 不会排除空字符串 |
| `data` | 泛型业务数据；为 `null` 时省略 |
| `count` | 可选的总记录数，为 `null` 时省略；分页时由应用设置 |
| `Duration` | 可选耗时，为 `null` 时省略；可由 MVC 的 `@Timing` 切面设置 |

示例直接引用参与 Maven 编译的源码：

<<< @/examples/src/main/java/cn/hiboot/mcn/docs/ResponseExamples.java

`writeResponse()` 的结果为：

```json
{
  "ActionStatus": "OK",
  "ErrorCode": 0,
  "ErrorInfo": "",
  "data": ["demo"],
  "count": 1
}
```

错误可以使用 `RestResp.error(code, message)`、`RestResp.error(code)` 或 `RestResp.error(message)` 创建。只传消息时错误码为 `999999`；只传错误码时，从 `ErrorMsg` 的消息表读取文本，未找到时是空字符串。

`isSuccess()`、`isFailed()` 等辅助方法不属于 JSON 响应字段。`remoteData()` 在失败响应上会抛出 `ServiceException`；带 `BiFunction<Integer, String, T>` 的重载可由调用方提供失败处理。

自动记录耗时需要显式配置 `mcn.execute-time.enabled=true`，在类或方法上使用 `cn.hiboot.mcn.autoconfigure.web.mvc.Timing`，并经过 Spring AOP 代理。当前 MVC advisor 仅对实现 `HttpTime` 的返回对象设置毫秒耗时（例如 `RestResp`），不会给任意返回对象增加字段，也不表示 WebFlux 异步处理的完整耗时。

## 常用工具

### JacksonUtils

`cn.hiboot.mcn.core.util.JacksonUtils` 提供 `toJson`、`toPrettyJson`、`toBytes`、`toBean`、`toList`、`toMap` 等方法。泛型类型引用使用 `tools.jackson.core.type.TypeReference`，对象映射器使用 `tools.jackson.databind.ObjectMapper`。

- 非 Spring 场景下首次调用会创建 `JsonMapper`，默认排除空值、忽略未知字段。
- 在 MCN 的 Spring 初始化流程中，`McnBeanPostProcessor` 处理 Jackson 3 `ObjectMapper` Bean，重建并注册子类型，再交给 `JacksonUtils.setObjectMapper`。
- 不能把 Jackson 2 的 `com.fasterxml.jackson.databind.ObjectMapper` 传给当前 API。Jackson 注解包没有随数据绑定 API 一起更名，不应机械替换所有 `com.fasterxml.jackson` 导入。
- 转换失败会抛出携带原始原因的 `ServiceException`，业务方应在适当边界处理。

### SpringBeanUtils

`SpringBeanUtils.getBean` 支持按类型、名称以及名称加类型获取 Bean。上下文由 MCN 监听器在 `ApplicationPreparedEvent` 阶段设置；未设置时调用会失败。

这是静态的单个上下文引用：当前引用为空时可以写入，传入带父上下文的子上下文时也会覆盖。它不按请求选择上下文，关闭上下文时也不会自动清空。常规业务组件优先使用构造器注入。

## 源码位置

- [RestResp](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-core/src/main/java/cn/hiboot/mcn/core/model/result/RestResp.java)
- [JacksonUtils](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-core/src/main/java/cn/hiboot/mcn/core/util/JacksonUtils.java)
- [McnBeanPostProcessor](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/context/McnBeanPostProcessor.java)

继续阅读[自动配置](./autoconfig.md)中的异常处理与参数处理说明。
