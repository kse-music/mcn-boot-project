# 自动配置

## 统一异常处理

### 三种使用方式
1. [**推荐**]直接使用异常ServiceException
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
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ExceptionResolver<HttpMessageNotReadableException> specialSymbolExceptionResolver() {
        return t -> {
            ServiceException serviceException = ServiceException.find(t);
            if(serviceException == null || serviceException.getCode() != ExceptionKeys.SPECIAL_SYMBOL_ERROR){
                return null;
            }
            return RestResp.error(serviceException.getCode(),serviceException.getMessage());
        };
    }
}
```

## Swagger配置

1. 默认swagger自动配置关闭,可通过swagger.enabled=true开启
2. 默认将带有注解RestController的接口生成文档忽略带有注解@IgnoreApi和@ApiIgnore的接口

## 跨域配置

1. 默认跨域不启动,可通过filter.cross.enabled=true启用跨域,方便开发调试
2. 参数说明
```properties
#配置允许的请求方式
mcn.cors.allowed-method=GET,POST
#配置允许的请求来源
mcn.cors.allowed-origin=https://www.xxx.com/
#配置允许的请求头
mcn.cors.allowed-header=TSM
#配置拦截的路径模式,默认/**即拦截所有
mcn.cors.pattern=/**
```

::: tip 提示
生产环境一定要关闭跨域设置
:::

## XSS配置

1. 默认跨域不启用,可通过mcn.xss.enabled=true启用
2. 如果使用security,默认顺序在安全过滤器链后执行,可通过mcn.xss.order=-101调整到其之前执行
3. 参数说明
```properties
#是否启用Xss过滤
mcn.xss.enabled=true
#存在xss是否直接抛异常,默认false
#mcn.xss.failed-fast=true
#指定哪些接口需要处理(ant匹配模式)
mcn.xss.exclude-urls=
#指定哪些字段不做处理(全局生效),如mcn.xss.exclude-fields=name,则所有接口中name字段不处理
mcn.xss.exclude-fields=
#指定哪些接口需要处理(ant匹配模式),默认/**
mcn.xss.include-urls=/**
```
::: tip 提示
生产环境建议开启XSS设置
:::

## WebSecurity配置

1. 当使用了SpringSecurity时mcn自动忽略以下路径

```properties
web.security.default-exclude-urls=/v2/api-docs,/swagger-resources/**,/doc.html,/webjars/**,/error,/favicon.ico,/_imagePreview,/*.png,/_groovyDebug_
```

2. 配置说明
```properties
#配置不拦截的路径
web.security.exclude-urls==/user/**,/news/add
```

::: tip 提示
可通过web.security.enable-default-ignore=false关闭默认忽略的路径
:::

## Validator配置

1. 配置了hibernate.validator.fail_fast=true，当第一个参数校验失败后续不校验
2. 扩展了分组校验区分校验时是否需要校验默认分组
3. 提供注解@Phone校验手机号

::: tip 提示

@Phone 仅简单校验了11位数字手机号

:::

## 多数据源配置
通过前缀multiple.datasource配置数据源即代表使用的是多数据源配置。

1. 配置多个数据源

```properties
multiple.datasource.hello.url=jdbc:mysql://127.0.0.1:3306/test?createDatabaseIfNotExist=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai
multiple.datasource.hello.username=root
multiple.datasource.hello.password=123456

multiple.datasource.world.url=jdbc:mysql://127.0.0.1:3306/web_template?createDatabaseIfNotExist=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai
multiple.datasource.world.username=root
multiple.datasource.world.password=123456
```

2. 启用动态数据源支持

```properties
dynamic.datasource.enabled=true
```

### 在MyBatis和Jpa使用动态数据源
1. 在Mybatis中使用
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

2. 在Jpa中使用
```xml
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

```
3. 使用注解SwitchSource切换数据源（**数据源名称就是multiple.datasource后的第一个字符串即hello和world**）

```java
@RequestMapping("test")
@RestController
@SwitchSource("hello")
public class TestRestApi {

    private UserDao userDao;
    
	public TestRestApi(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("list")
    public RestResp<List<User>> list() {
        return new RestResp<>(userDao.findAll());
    }

    @GetMapping("list2")
    @SwitchSource("world")
    public RestResp<List<User>> list2() {
        return new RestResp<>(userDao.findAll());
    }
}    
```

::: tip 提示
SwitchSource注解既可以用在类上也可以用在方法上，方法上的优先级高
:::


### MyBatis和Jpa使用多数据源
1. 使用多数据源
```properties
#同时只能启动一个
#mybatis.multiple.datasource.enabled=true
jpa.multiple.datasource.enabled=true
```
2. 数据访问层位置
> dao层必须在启动类所在包的子包dao下且用数据源的名称当子包名称，如下图所示
<img :src="$withBase('/images/d.png')" alt="数据访问层位置">

3. 使用
```java
@RequestMapping("test")
@RestController
public class TestRestApi {
    private UserDao userDao;
    private UserDao2 userDao2;

    public TestRestApi(UserDao userDao, UserDao2 userDao2) {
        this.userDao = userDao;
        this.userDao2 = userDao2;
    }

    @GetMapping("list3")
    public RestResp<List<User>> list3() {
        List<User> all = userDao2.findAll();
        all.addAll(userDao.findAll());
        return new RestResp<>(all);
    }
}  
```

::: warning 注意
1. 动态数据源开关和jpa多数据源开关以及mybatis多数据源开关三者同时只能开启一个
2. 当三个开关都没开启时，默认会使用动态数据源模式
3. jpa和mybatis的多数据源配置基本一样，引入不同的依赖就行了
:::

## 传输加解密
### 使用SM2加密配置
使用方式
1. 引入依赖并配置密钥对
```xml
<dependencies>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
    </dependency>

    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-crypto</artifactId>
    </dependency>
</dependencies>
```
以下配置建议放在classpath:config/mcn.properties文件中
```properties
encryptor.sm2.private-key=308193020100301306072a8648ce3d020106082a811ccf5501822d04793077020101042044880abf2572c0946e4d7a18a812fe554f20db40bc852a3b78b7e057af72344ca00a06082a811ccf5501822da14403420004258785f13a181c19fe366f13e1e4d93834944fb6b2d05b0ef58963e3cbdc3d680b228bdeab895a1f113dd6690279cd932ab85ecf694ebf34d6201b9d76055094
encryptor.sm2.public-key=3059301306072a8648ce3d020106082a811ccf5501822d03420004258785f13a181c19fe366f13e1e4d93834944fb6b2d05b0ef58963e3cbdc3d680b228bdeab895a1f113dd6690279cd932ab85ecf694ebf34d6201b9d76055094
```

2. 接口中使用

```java

@RequestMapping("test")
@RestController
public class TestRestApi {

    private final TextEncryptor textEncryptor;

    public TestRestApi(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @GetMapping("encrypt")
    public RestResp<String> encrypt(String text) {
        return new RestResp<>(textEncryptor.encrypt(text));
    }

    @GetMapping("decrypt")
    public RestResp<String> decrypt(String text) {
        return new RestResp<>(textEncryptor.decrypt(text));
    }

}
```

3. 数据模型中使用

只需要在需要解密的参数上添加注解@Decrypt即可
```java
@RequestMapping("test")
@RestController
public class TestRestApi {

    @GetMapping("list")
    public RestResp<String> list(@Decrypt String query) {
        return new RestResp<>(query);
    }

}

```
4. json反序列化中使用

只需要在需要解密的字段上添加注解@Decrypt即可
```java
@RequestMapping("test")
@RestController
public class TestRestApi {

    @PostMapping("add")
    public RestResp<User> add( @RequestBody User userBean) {
        return new RestResp<>(userBean);
    }

}

@Setter
@Getter
public class User{

    private Integer id;

    @Decrypt
    private String name;

    private Integer age;

}

```

## 完整性校验
### 使用SM3计算摘要
使用方式
1. 引入依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
    </dependency>

    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-crypto</artifactId>
    </dependency>
</dependencies>
```

2. 配置说明
```properties
#开启完整性校验
data.integrity.enabled=true
#指定哪些接口不需要校验(ant匹配模式)
data.integrity.exclude-patterns=
#指定哪些接口需要校验(ant匹配模式),默认/**
data.integrity.include-patterns=/**
#指定是否在feign调用中也校验,默认false不校验
#data.integrity.interceptor.enabled=false
```

3. 接口中使用

在header里出入以下三个参数timestamp(时间戳)、nonceStr(随机字符串)、signature(签名)
```java
@RequestMapping("test")
@RestController
public class TestRestApi {

    @GetMapping("list")
    public RestResp<String> list(String query) {
        return new RestResp<>(query);
    }

}
```
::: tip 提示
如果不在header里传入相关参数或者校验失败会提示
```json
{
    "ActionStatus": "FAIL",
    "ErrorCode": 999999,
    "ErrorInfo": "验证失败,数据被篡改"
}
```
:::

## 配置加解密
### 使用SM4加密配置
使用方式
1. 引入依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
    </dependency>

    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-crypto</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bootstrap</artifactId>
    </dependency>
</dependencies>
```

2. 配置加密key

以下配置必须放在classpath:config/mcn.properties文件中
```properties
#加密key,必须128位即16字节
encryptor.sm4.key=abcdefghijklmnop
```
3. 配置中使用

如在application.properties中存在加密项spring.datasource.username,而真正使用的是解密后的值root
```properties
spring.datasource.username={cipher}7eda434344898ba11617084e0f117103
```

4. 接口中使用

```java
@RequestMapping("test")
@RestController
public class TestRestApi {
    
    private UserDao userDao;
    private TextEncryptor textEncryptor;

    public TestRestApi(UserDao userDao, TextEncryptor textEncryptor) {
        this.userDao = userDao;
        this.textEncryptor = textEncryptor;
    }

    @PostMapping("add")
    public RestResp<User> add( @RequestBody User userBean) {
        //入库手机号加密
        userBean.setMobile(textEncryptor.encrypt(userBean.getMobile()));
        userDao.save(userBean);
        return new RestResp<>(userBean);
    }

}

```
::: warning 注意
1. sm4是对称加密一般用于后端配置项及入库数据加密
2. 密钥key必须放在classpath:config/mcn.properties文件中
:::

## 参数预处理
对输入的参数的统一处理,支持kv编码和json编码参数

1. 参数说明
```properties
#是否启用参数处理
param.processor.enabled=true
#指定哪些接口需要校验(ant匹配模式)
param.processor.exclude-urls=
#指定哪些字段不做处理(全局生效),如param.processor.exclude-fields=name,则所有接口中name字段不处理
param.processor.exclude-fields=
#指定哪些接口需要校验(ant匹配模式),默认/**
param.processor.include-urls=/**
#全局校验规则(正则表达式),默认的参数处理器ParamProcessor是基于正则的
global.rule.pattern=.*
```

2. 自定义参数处理器

```java
@Component
public class CustomRule{

    @Bean
    public ParamProcessor myParamProcessor() {
        return (rule, name, value) -> {
            //这里可以编写校验逻辑
            //rule:当前规则
            //name:当前字段名
            //value:当前字段值
            Pattern pattern = Pattern.compile(rule);
            if(pattern.matcher(value).matches()){
                throw ServiceException.newInstance(ExceptionKeys.SPECIAL_SYMBOL_ERROR);
            }
            return value;
        };
    }
}

```

3. 各字段自定义规则(一般用不到)

通过在参数上指定注解@CheckParam来定义各字段不同的校验规则

```java

@Setter
@Getter
@CheckParam("[a-z0-9]+")
public class User{

	private Integer id;
    @CheckParam("[a-z]+")
	private String name;

	private Integer age;

}

```

## 分布式锁

1. 引入依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```
2. 示例

    默认获取锁及锁持有的时间都是5秒。

```java
//方式一：使用注解@DistributedLock
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
```java
//方式二：直接使用DistributedLocker接口
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
        //方式一:需要手动释放锁
        if(distributedLocker.tryLock(LIST_LOCK_NAME,10,10)){//获取锁等待超时10秒持有超时时间10秒
            //do something
            distributedLocker.unlock(LIST_LOCK_NAME);
        }
        //方式二:自动释放锁无返回值
        distributedLocker.tryExecute(LIST_LOCK_NAME,10,() -> {
            //do something

        });
        //方式三:自动释放锁有返回值
        String value = distributedLocker.tryExecute(LIST_LOCK_NAME,10,() -> {
            //do something
            return "value";
        });
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
3. 在classpath:db/下分别创建schema-mysql.sql、schema-kingbase8.sql、other-kingbase8.sql文件

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

1. 由于kingbase不支持createDatabaseIfNotExist参数，所以kingbase需要手动先建数据库或者**指定一个已存在的库来自动建库**
```properties
#系统会与该init_db建立连接再创建test库
spring.sql.init.additional.init-db-name=init_db
```
2. 由于kingbase不支持ON UPDATE CURRENT_TIMESTAMP,所以需要使用触发器实现
3. 由于触发器脚本不能以;分隔，所以需要指定一个其它的分隔符，默认//

:::

4. 切换使用kingbase数据库
* 修改配置项spring.sql.init.platform=kingbase8
* [**推荐**]在系统属性或环境变量中指定spring.sql.init.platform=kingbase8
* [**推荐**]若使用配置中心(如：nacos)则直接在配置中心指定spring.sql.init.platform=kingbase8