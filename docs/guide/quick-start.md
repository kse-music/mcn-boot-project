# 快速开始

## 新建Maven项目

1. 引入父模块

```xml
<parent>
    <artifactId>mcn-boot-starter-parent</artifactId>
    <groupId>cn.hiboot.mcn</groupId>
    <version>${最新稳定版}</version>
</parent>
```
2. 添加依赖

```xml
<dependencies>

    <dependency>
        <groupId>cn.hiboot.mcn</groupId>
        <artifactId>mcn-spring-boot-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>cn.hiboot.mcn</groupId>
        <artifactId>mvc-swagger2</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>

</dependencies>
```

3. 添加maven插件

```xml
<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <skipTests>true</skipTests>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <configuration>
                <archive>
                    <manifestEntries>
                        <!--suppress UnresolvedMavenProperty -->
                        <Build-Timestamp>${timestamp}</Build-Timestamp>
                        <Implementation-Version>${project.version}</Implementation-Version>
                    </manifestEntries>
                </archive>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>buildnumber-maven-plugin</artifactId>
            <configuration>
                <timestampFormat>yyyy-MM-dd HH:mm:ss</timestampFormat>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>create-timestamp</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 组织包结构

1. 创建base package，如cn.hiboot.demo，并创建一个SpringBoot应用启动类，如：DemoApplication
```java
package cn.hiboot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类
 *
 * @author DingHao
 * @since 2020/5/27 10:19
 */
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```
2. 创建rest包，如cn.hiboot.demo.rest，并创建DemoRestApi,内容如下
```java
package cn.hiboot.demo.rest;

import cn.hiboot.demo.bean.DemoBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * rest接口
 *
 * @author DingHao
 * @since 2020/5/27 10:20
 */
@RequestMapping("demo")
@RestController
@Validated
@Api(tags = "demo接口")
public class DemoRestApi {

    @GetMapping("list")
    @ApiOperation("列表")
    public RestResp<String> list(String query) {
        return new RestResp(query);
    }

    @PostMapping("json")
    @ApiOperation("post json")
    public RestResp<DemoBean> postJson(@Validated @RequestBody DemoBean userBean) {
        return new RestResp(userBean);
    }
}
```

3. 在src/java/resources下新建config文件夹，再在里面新建一个application.properties并写入以下内容

```properties
#一般与项目模块对应
spring.application.name=demo
#开启swagger
swagger.enable=true
```

## 运行&访问

1. 运行DemoApplication
2. 访问[SwaggerUI](http://127.0.0.1:8080/doc.html)