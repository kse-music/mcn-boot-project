# 扩展模块

系统内置三种属性源名称分别是：mcn-global-unique、mcn-map、mcn-default

## mcn-global-unique
1. 只要EnvironmentPostProcessor触发就自动加载classpath:config/mcn.properties文件。无论当前是否是引导上下文。

::: tip 提示

当启用了spring.profiles.active配置同时会加载文件mcn-{profile}.properties,当激活多个profile时且存在相同属性配置时后面的优先级比前面的高

:::

## mcn-map
该属性源里只包含五个配置(内部使用)：

1. app.base-package:项目启动的根路径即Application所在package
2. logging.level.{app.base-package}.dao:设置dao包下的日志级别为info
3. project.version:项目版本号读取manifest文件(如果存在)
4. mcn.log.file.name:日志输出的文件名默认为error(注意：不包含扩展名)
5. mcn.version:mcn版本号读取manifest文件

## mcn-default

该属性源里包含很多配置如:

```properties
#内嵌tomcat根路径
server.tomcat.basedir=/tmp/tomcat/
#启用tomcat数据压缩
server.compression.enabled=true

```
[全部配置](https://github.com/kse-music/mcn-boot-project/blob/master/mcn-boot-autoconfigure/src/main/java/cn/hiboot/mcn/autoconfigure/config/mcn-default.properties)

::: warning 注意

除了mcn-global-unique之外其它属性源默认不会加引导上下文中加载

1. 当配置mcn.bootstrap.eagerLoad.enable=true可启用其它三个属性源也在引导上下文中加载

2. 当环境中存在mcn-default属性源，则mcn-default、mcn-map属性源都不会加载

3. 日志文件按天压缩，自动删除30天前日志

:::

## 属性源打印

调试时使用,生产应关闭！！！

当环境中配置mcn.print-env.enable=true时,项目在启动时会自动打印所有可枚举的属性源。