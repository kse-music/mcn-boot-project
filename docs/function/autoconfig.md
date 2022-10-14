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

自定义一个处理HttpMessageNotReadableException的异常处理器。

```java
@Configuration
public class CustomExceptionResolver {
    @Bean
    public ExceptionResolver specialSymbolExceptionResolver() {
        return new ExceptionResolver() {

            @Override
            public boolean support(HttpServletRequest request, Throwable t) {
                return t instanceof HttpMessageNotReadableException;
            }

            @Override
            public RestResp<Object> resolveException(HttpServletRequest request, Throwable t) {
                ServiceException serviceException = ServiceException.find(t);
                if (serviceException == null || serviceException.getCode() != ExceptionKeys.SPECIAL_SYMBOL_ERROR) {
                    return null;
                }
                return RestResp.error(serviceException.getCode(), serviceException.getMessage());
            }

        };
    }
}
```

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
默认获取锁及锁持有的时间都是5秒。

### 方式一：使用注解@DistributedLock
示例：
```java

@RequestMapping("test")
@RestController
public class TestRestApi {

    @GetMapping("list")
    @DistributedLock("list_lock")
    public RestResp<String> list(String query) {
        return new RestResp<>(query);
    }

}

```

### 方式二：直接使用DistributedLocker接口
示例：
```java

@RequestMapping("test")
@RestController
public class TestRestApi {

    private static final String LIST_LOCK_NAME = "list_lock";
    private final DistributedLocker distributedLocker;

    public TestRestApi(DistributedLocker distributedLocker) {
        this.distributedLocker = distributedLocker;
    }

    @GetMapping("list")
    @DistributedLock("list_lock")
    public RestResp<String> list(String query) {
        if(distributedLocker.tryLock(LIST_LOCK_NAME,10,10)){//获取锁等待超时10秒持有超时时间10秒
            //do something
            distributedLocker.unlock(LIST_LOCK_NAME);
        }
        return new RestResp<>(query);
    }

}


```

## War包模式

1. 在classpath的config目录下创建mcn.properties文件 
2. 添加main.class=应用启动类(即main方法所在类) 

## 数据库初始化

扩展原有的数据库初始化即默认情况处理classpath*下的db文件夹下的脚本,另额外添加文件名为other的sql文件解析并可指定一个分隔符。

示例：自动初始化一张表并须支持不同环境，如MySQL和kingbase的初始化脚本。

1. pom.xml添加驱动
```xml
<dependencies>

     <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.kingbase8</groupId>
        <artifactId>kingbase8</artifactId>
        <version>8.6.0</version>
    </dependency>
    
</dependencies>

```
2. 在application配置文件添加以下配置

```properties
spring.datasource.url=jdbc:${spring.sql.init.platform}://127.0.0.1:3306/test?createDatabaseIfNotExist=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=111111

spring.sql.init.mode=always
#指定当前应用运行环境使用的是mysql
spring.sql.init.platform=mysql
spring.sql.init.continue-on-error=true

```
3. 在classpath:db/下分别创建schema-mysql.sql、schema-kingbase8.sql、other-kingbase8.sql
schema-mysql.sql内容如下：
```sql
CREATE TABLE t_user (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(64) NOT NULL,
  age int(11) DEFAULT NULL,
  create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```
schema-kingbase8.sql内容如下：
```sql
CREATE TABLE t_user (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  age INT DEFAULT NULL,
  create_at timestamp DEFAULT sysdate NOT NULL,
  update_at timestamp DEFAULT sysdate NOT NULL,
  PRIMARY KEY ("id")
);
```
other-kingbase8.sql内容如下：
```sql
create or replace trigger t_user_trigger
before
update on t_user
    for each row
begin
select sysdate into :NEW.update_at from dual;
end;
//
    
```
::: tip 提示

1. 由于kingbase不支持createDatabaseIfNotExist参数，所以kingbase需要手动先建数据库
2. 由于kingbase不支持ON UPDATE CURRENT_TIMESTAMP,所以需要使用触发器实现
3. 由于触发器脚本不能以;分隔，所以需要指定一个其它的分隔符，默认使用的是//

:::

4. 切换使用kingbase数据库
* 修改配置项spring.sql.init.platform=kingbase8
* [**推荐**]在系统属性或环境变量中指定spring.sql.init.platform=kingbase8
* [**推荐**]若使用配置中心(如：nacos)则直接在配置中心指定spring.sql.init.platform=kingbase8