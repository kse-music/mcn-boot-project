# 通用模块

### 统一数据结构

```java
package cn.hiboot.mcn.core.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)//Null字段不返回
public class RestResp<T> {

    public enum ActionStatusMethod {
        OK,
        FAIL
    }

    @JsonProperty("ActionStatus")//返回字段名大写,默认OK表示正常结果返回
	private ActionStatusMethod ActionStatus = ActionStatusMethod.OK;

    @JsonProperty("ErrorCode")//返回字段名大写,默认0表示无错误
	private Integer ErrorCode = 0;

    @JsonProperty("ErrorInfo")//返回字段名大写,错误具体信息(当异常返回时)
	private String ErrorInfo = "";

	@JsonProperty("Duration")//接口执行时间需结合注解@Timing使用
	private Long duration;

	private T data;//接口返回的数据

	private Long count;//数据返回的count数,分页时使用

    //省略set/get
}
```

### 常用工具

1. JacksonUtils(可在非spring环境中使用)-基于jackson的一个序列化和反序列化工具

::: warning 注意

当在SpringBoot项目里使用时,IOC容器中存在ObjectMapper则优先使用外部的

:::

2. SpringBeanUtils-一个方便在静态方法中从IOC容器获取bean的工具

::: warning 注意

当存在多个上下文时且当前上下文是子上下文时会更新applicationContext

:::