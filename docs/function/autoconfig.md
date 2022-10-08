# 自动配置

## 统一异常处理

### 三种使用方式
1. 直接使用异常ServiceException(推荐)
```java
public class UserService{
    
    private UserMapper userMapper;
    
    public UserService(UserMapper userMapper){
        this.userMapper = userMapper;
    }
    
    public void login(String username,String password){
        if(userMapper.find(username) == null){
            throw ServiceException.newInstance("用户不存在");
        }
    }
    
}
```

2. 直接自定义异常继承BaseException
```java
public class CustomException extends BaseException{
    public CustomException(Throwable cause) {
        super(cause);
    }
}
```

3. 直接自定义异常继承RuntimeException
```java
public class CustomException extends RuntimeException{
    
}
```

::: warning 注意
1. 如果抛出的BaseException子类异常无错误码则使用默认错误码999999
2. 如果抛出非BaseException子类异常则使用默认错误码999998
3. 如果抛出异常信息的同时指定了错误码则不使用默认的错误码
4. 如果只抛出错误码则从classpath:error-msg.properties中获取,获取不到则使用默认的内部服务器错误信息
:::

### 自定义异常解析器

## Swagger配置

1. 默认swagger自动配置关闭,可通过swagger.enable=true开启
2. 默认将带有注解RestController的接口生成文档忽略带有注解@IgnoreApi和@ApiIgnore的接口

## 跨域配置

1. 默认跨域不启动,可通过filter.cross=true启用跨域,方便开发调试

::: warning 注意
生产环境一定要关闭跨域设置
:::

## XSS配置

1. 默认跨域不启用,可通过mcn.xss.enable=true启用
2. 如果使用security,默认顺序在安全过滤器链后执行,可通过mcn.xss.order=-101调整到其之前执行

::: warning 注意
生产环境建议开启XSS设置
:::

## WebSecurity配置

1. 当使用了SpringSecurity时mcn自动忽略以下路径

```properties
web.security.default-exclude-urls=/v2/api-docs,/swagger-resources/**,/doc.html,/webjars/**,/error,/favicon.ico,/_imagePreview,/*.png,/_groovyDebug_
```

::: tip 提示
可通过web.security.enable-default-ignore=false关闭默认忽略，也可以通过web.security.exclude-urls=/a/b,/c/**添加更多放行路径
:::

## Validator配置

1. 配置了hibernate.validator.fail_fast=true，当第一个参数校验失败后续不校验
2. 扩展了分组校验区分校验时是否需要校验默认分组
3. 提供注解@Phone校验手机号

::: warning 注意

@Phone 仅简单校验了11位数字手机号

:::

## 多数据源配置

### Mybatis多数据源
### Jpa多数据源

## 配置加解密

### 使用SM4加密配置

## 完整性校验

## 参数预处理


## 分布式锁

### 使用注解@DistributedLock

### 编程使用DistributedLocker接口

## War包模式