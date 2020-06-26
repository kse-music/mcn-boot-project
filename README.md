### 一、简介
MCN是一个基于SpringBoot和Jersey的一个快速构建Restful风格的系统。

SpringBoot的横空出世不外乎就是之前的配置繁杂，但其实SpringBoot不算是一个严格意义上的框架（Spring是）。框架从字面上理解就是“框注”你，即遵循框架制定的规则来“玩游戏”！其实，SpringBoot就是把以前我们自己在开发时需要的一些配置，它帮我们配置好了，从而简化了开发。

```
graph LR
Spring-->SpringBoot
SpringBoot-->SpringCloud
```

### 二、定位
其实MCN的出发点和SpringBoot是一致的，它是在SpringBoot的基础上配置的更多，比如mybatis多数据源配置、跨域、编码等等一些SpringBoot没有的或者扩展的配置。



### 三、小试牛刀

1.新建一个空的Maven项目
- 引入父模块

```
    <parent>
        <artifactId>mcn-boot-parent</artifactId>
        <groupId>cn.hiboot.mcn</groupId>
        <version>2.3.1</version>
    </parent>
```

- 引入相关依赖lib

```
	<dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jersey</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.hiboot.mcn</groupId>
            <artifactId>jersey-swagger2-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.hiboot.mcn</groupId>
            <artifactId>mcn-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <scope>provided</scope>
        </dependency>
        
	</dependencies>
```

- 创建base package，如com.hiekn.test，并在其下创建一个Spring应用启动类，如：TestApplication，并写入以下信息

```
package com.hiekn.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class,args);
    }
}

```

- 在src/java/resources下新建config文件夹，再在里面新建一个application.properties并写入以下内容

```
#根据实际项目修改
spring.application.name=test

#根据实际项目修改
spring.datasource.url=jdbc:mysql://192.168.1.159:3306/test?characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false
spring.datasource.username=root
spring.datasource.password=root
```

2.自动生成基础CRUD代码
- 将制mcn里面的generatorConfig.xml复制到src/main/resources下

- 引入maven插件，示例如下

```
<plugin>
    <groupId>com.hiekn.generator</groupId>
    <artifactId>mcn-maven-plugin</artifactId>
    <configuration>
        <propConfig>
            <property>
                <name>db.driver</name>
                <value>com.mysql.jdbc.Driver</value>
            </property>
            <property>
                <name>db.url</name>
                <value>jdbc:mysql://192.168.1.159:3306/test?characterEncoding=utf8&amp;autoReconnect=true&amp;failOverReadOnly=false&amp;useSSL=false</value>
            </property>
            <property>
                <name>db.user</name>
                <value>root</value>
            </property>
            <property>
                <name>db.password</name>
                <value>root@hiekn</value>
            </property>
            <property>
                <name>basePkg</name>
                <value>com.hiekn.test</value>
            </property>
        </propConfig>
    </configuration>
</plugin>
```

- 使用generatorConfig.xml自动生成代码，只需要在context标签最下面添加要自动生成映射的表名，示例如下

```
    <!-- 
    tableName：表名
    domainObjectName： 数据模型名
    mapperName： mybatis mapper文件名
    -->
    <table tableName="tb_news" domainObjectName="NewsBean" mapperName="NewsMapper"
           enableCountByExample="false" enableUpdateByExample="false"
           enableDeleteByExample="false" enableSelectByExample="false"
           selectByExampleQueryId="false">
           <!-- 注解id自增-->
        <generatedKey column="id" sqlStatement="JDBC"/>
    </table>
```

- 点击IDEA maven插件mcn:genrate，生成所有代码
- 运行TestApplication
- 浏览器打开[http://127.0.0.1:8080/api/Swagger.html](http://127.0.0.1:8080/api/Swagger.html)即可查看API接口
- 可直接在swagger测试每一个接口了


3.注意事项

-【**重要**】数据库设计需按规范设计，db名称、表名称、字段名称如遇多个单词的以下划线分隔

-【**建议**】所有表设计必须含有create_time和update_time字段,且这两个字段交给数据自己维护，如果不需要这两个字段也没关系，只要把sql中排序删掉即可

```
create_time表示数据的添加时间（永久不变）
update_time表示最后修改时间（随数据的每一次修改而改变）
```

-【**重要**】所有变量命名，不管是java还是数据库的都不要出现单个字母打头的驼峰或者下划线、横线的组合词

### 四、功能展示
1.elasticsearch client配置
- 引入依赖

```
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>transport</artifactId>
    </dependency>

    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
    </dependency>
```
- 使用

2.jwt-token配置
- 引入依赖

```
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>java-jwt</artifactId>
    </dependency>
```
- 使用
 
3.mybatis单数据源配置和多数据源配置
- 引入依赖

```
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
    </dependency>
```
- 使用
 
4.jersey-swagger配置
- 引入依赖

```
    <dependency>
        <groupId>com.hiekn.boot</groupId>
        <artifactId>mcn-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jersey</artifactId>
    </dependency>
    <dependency>
        <groupId>com.hiekn.boot</groupId>
        <artifactId>mcn-swagger2-ui</artifactId>
    </dependency>

```
- 使用
 
6.validator使用
- 引入依赖

```
   <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jersey</artifactId>
    </dependency>
```
- 使用
 
7.日志配置

8.系统默认配置

```

spring.jersey.type=filter

#mybatis数据模型默认别名包
mybatis.type-aliases-package=${app.base-package}.bean
#mybatis，mapper文件默认路径
mybatis.mapper-locations=classpath:mapper/*.xml
#mybatis，handlers默认包
mybatis.type-handlers-package=${app.base-package}.dao.handler
#mybatis，默认使用驼峰
mybatis.configuration.map-underscore-to-camel-case=true

#默认服务器路径
server.tomcat.basedir=/tmp/tomcat/
server.compression.enabled=true

#log，默认日志配置
logging.file.name=${logging.baseDir:/work/logs}/${spring.application.name:none}/${mcn.log.file.name}.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} %green(%-5level) %magenta(${PID:- }) --- [%15thread] %cyan(%-40logger{39}) %-5L : %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level ${PID:- } --- [%15thread] %-40logger{39} %-5L : %msg%n
logging.config=classpath:META-INF/logback-spring.xml
logging.level.root=info
logging.level.org=warn
logging.level.io=warn
logging.file.max-history=30
#文件输出过滤器掉info以下
mcn.log.filter=info

#默认api版本，读取pom version
jersey.swagger.version=@project.version@
#默认title
jersey.swagger.title=${spring.application.name} API
jersey.swagger.ip=127.0.0.1
jersey.swagger.port=${server.port:8080}
jersey.swagger.base-path=${spring.jersey.application-path:}
#默认jersey资源包
jersey.swagger.resource-package=${app.base-package}.rest

spring.freemarker.check-template-location=false
lic.white=h/lic,swagger.json

swagger.title=${spring.application.name} API
swagger.version=@project.version@

```


### 五、注意事项
1.spring.jersey.application-path一定要设置一个非“/”的值，一般不用改，自定义路径统一走网关。

```
理由：改了之后会把内置swagger的静态资源也拦截了
```

2.MCN统一管理SpringBoot和SpringCloud的版本以及一些出此自外的一些jar版本，详细如下表所示：

Jar | Version | 备注
---|---|---
SpringBoot | 2.2.6.RELEASE | 
SpringCloud  | Hoxton.SR3 | 
swagger-jersey2-jaxrs  | 1.5.19 | 
guava  | 29.0-jre | 
mybatis-spring-boot-starter  | 2.1.2 | 
commons-lang3  | 3.7 | 
commons-io  | 2.6 | 
spring-boot-admin-starter-client  | 2.2.1 | 
spring-boot-admin-starter-server  | 2.2.1 | 
rest  | 6.6.2 | elasticsearch
elasticsearch  | 7.6.2 | 全文本检索
java-jwt | 3.1.0 | 
mybatis-generator-core | 1.3.6 | 
fastjson | 1.2.68 | 


3.所有的默认配置都可以覆盖

4.所有功能都是可选的，包括jersey本身，即开发一个非web的项目也可以引入mcn。

5.重要的事情说三遍
- ==用不到jar一定要去掉==
- ==用不到jar一定要去掉==
- ==用不到jar一定要去掉==


**为什么用不到jar一定要去掉？**

>因为在SpringBoot出现之前，我们开发依赖的jar包都很随意的瞎引，好像也没什么事！然而如今，用了SpringBoot之后就不一样了。拿mongo来距离，可能是由习惯问题吧，很多人就还和以前一样自己去写mongoclient单例，然后去对mono做CRUD操作。这时就发现启动项目之后，一段时间（超时）就报mongo连接超时！原来是在你引入mongo-java之后SpringBoot的自动配置就开始默认连接127.0.0.1 27017的地址，导致失败、资源浪费！这也是我个人喜欢SpringBoot的原因之一。



### 六、相关链接
1.[SpringBoot启动源码分析](http://www.hiboot.cn/jie/83)

2.SpringCloud引导上下文源码分析

3.一个基于MCN的完整项目示例[meta-boot](https://github.com/kse-music/meta-boot)