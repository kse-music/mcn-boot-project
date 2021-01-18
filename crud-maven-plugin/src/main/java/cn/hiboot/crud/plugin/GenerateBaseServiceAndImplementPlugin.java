package cn.hiboot.crud.plugin;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.beans.Introspector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class GenerateBaseServiceAndImplementPlugin extends PluginAdapter {
    private String serviceTargetDir;
    private String serviceTargetPackage;
    private String service;
    private boolean overwrite;
    private boolean isJersey;

    private ShellCallback shellCallback;

    public GenerateBaseServiceAndImplementPlugin() {
        shellCallback = new DefaultShellCallback(true);
    }

    @Override
    public boolean validate(List<String> warnings) {
        serviceTargetDir = properties.getProperty("targetProject");
        serviceTargetPackage = properties.getProperty("targetPackage");
        service = properties.getProperty("service");
        overwrite = Boolean.parseBoolean(properties.getProperty("overwrite"));
        isJersey = Boolean.parseBoolean(properties.getProperty("isJersey"));
        return stringHasValue(serviceTargetDir) && stringHasValue(serviceTargetPackage) && stringHasValue(service);
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> javaFiles = new ArrayList<>();
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {
            CompilationUnit unit = javaFile.getCompilationUnit();
            FullyQualifiedJavaType baseModelJavaType = unit.getType();
            String shortName = baseModelJavaType.getShortName();

            if (shortName.endsWith("Mapper")) {
                //create interface XxxService FullName
                String serviceInterfaceFullName = serviceTargetPackage + "." + shortName.replace("Mapper", "Service");
                Interface serviceInterface = new Interface(serviceInterfaceFullName);
                serviceInterface.setVisibility(JavaVisibility.PUBLIC);

                String pk = "Object";//default PK Object
                List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
                if(primaryKeyColumns != null && primaryKeyColumns.size() == 1){
                    for (IntrospectedColumn keyColumn : primaryKeyColumns) {
                        pk = keyColumn.getFullyQualifiedJavaType().getShortName();
                    }
                }

                String rootInterface = properties.getProperty("rootInterface");

                String implServicePkg = serviceTargetPackage+".impl";
                TopLevelClass topLevelClass = new TopLevelClass(new FullyQualifiedJavaType(implServicePkg+"."+shortName.replace("Mapper", "ServiceImpl")));
                topLevelClass.setVisibility(JavaVisibility.PUBLIC);
                topLevelClass.addImportedType(service);
                topLevelClass.addImportedType(serviceInterfaceFullName);
                topLevelClass.addSuperInterface(new FullyQualifiedJavaType(serviceInterfaceFullName));
                topLevelClass.getAnnotations().add("@Service");

                if(rootInterface != null){
                    //create super interface BaseService
                    FullyQualifiedJavaType baseService = new FullyQualifiedJavaType("BaseService<"
                            + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ","
                            + pk+ ">");
                    serviceInterface.addImportedType(new FullyQualifiedJavaType(rootInterface));
                    serviceInterface.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
                    serviceInterface.addSuperInterface(baseService);

                    //create super class BaseServiceImpl
                    FullyQualifiedJavaType baseServiceImpl = new FullyQualifiedJavaType("BaseServiceImpl<"
                            + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ","
                            + pk+ ">");
                    topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
                    topLevelClass.addImportedType(rootInterface.replace("BaseService","BaseServiceImpl"));
                    topLevelClass.setSuperClass(baseServiceImpl);
                }
                String restPkg = serviceTargetPackage.replace(".service",".rest");
                TopLevelClass restClass = new TopLevelClass(new FullyQualifiedJavaType(restPkg+"."+shortName.replace("Mapper", "RestApi")));
                restClass.setVisibility(JavaVisibility.PUBLIC);
                restClass.addImportedType("org.springframework.stereotype.Controller");
                restClass.addImportedType("javax.validation.constraints.NotNull");
                restClass.addImportedType("javax.ws.rs.*");
                restClass.addImportedType("javax.ws.rs.core.MediaType");
                restClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
                restClass.addImportedType("javax.ws.rs.core.MediaType");
                restClass.addImportedType("io.swagger.annotations.Api");
                restClass.addImportedType("io.swagger.annotations.ApiOperation");
                restClass.addImportedType("cn.hiboot.mcn.core.model.result.RestResp");
                restClass.addImportedType("cn.hiboot.mcn.core.util.JsonUtils");
                restClass.addImportedType("javax.validation.Valid");
                restClass.addImportedType(serviceInterfaceFullName);
                restClass.addImportedType(introspectedTable.getBaseRecordType());
                restClass.getAnnotations().add("@Controller");
                String path = Introspector.decapitalize(shortName.replace("Mapper", ""));
                restClass.getAnnotations().add("@Path(\""+path+"\")");
                restClass.getAnnotations().add("@Produces(MediaType.APPLICATION_JSON)");
                restClass.getAnnotations().add("@Api(\""+shortName.replace("Mapper", "RestApi")+"\")");
                String xService = Introspector.decapitalize(shortName.replace("Mapper", "Service"));
                Field field = new Field(xService, new FullyQualifiedJavaType(serviceInterfaceFullName));
                field.getAnnotations().add("@Autowired");
                field.setVisibility(JavaVisibility.PRIVATE);
                restClass.getFields().add(field);
                generateRestApi(introspectedTable,restClass,xService,pk);

                try {
                    JavaFormatter javaFormatter = context.getJavaFormatter();
                    //gen XxxService
                    checkAndAddJavaFile(new GeneratedJavaFile(serviceInterface, serviceTargetDir, javaFormatter),javaFiles,serviceTargetPackage);
                    //gen XxxServiceImpl
                    checkAndAddJavaFile(new GeneratedJavaFile(topLevelClass, serviceTargetDir, javaFormatter),javaFiles,implServicePkg);
                    if(isJersey){
                        //gen XxxRestApi
                        checkAndAddJavaFile(new GeneratedJavaFile(restClass, serviceTargetDir, javaFormatter),javaFiles,restPkg);
                    }
                } catch (ShellException e) {
                    e.printStackTrace();
                }
            }
        }
        return javaFiles;
    }

    private void checkAndAddJavaFile(GeneratedJavaFile javaFile, List<GeneratedJavaFile> javaFiles, String pkg) throws ShellException {
        File dir = shellCallback.getDirectory(serviceTargetDir, pkg);
        File file = new File(dir, javaFile.getFileName());
        if(file.exists()){
            if(overwrite){
                javaFiles.add(javaFile);
            }
        }else{
            javaFiles.add(javaFile);
        }
    }


    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        try {
            java.lang.reflect.Field field = sqlMap.getClass().getDeclaredField("isMergeable");
            field.setAccessible(true);
            field.setBoolean(sqlMap, !overwrite);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void generateRestApi(IntrospectedTable introspectedTable, TopLevelClass restClass, String xService, String pk){
        String bean = Introspector.decapitalize(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        String Bean = introspectedTable.getFullyQualifiedTable().getDomainObjectName();

        Method listByPage = new Method("listByPage");
        listByPage.getAnnotations().add("@GET");
        listByPage.getAnnotations().add("@Path(\"list/page\")");
        listByPage.getAnnotations().add("@ApiOperation(\"分页\")");
        listByPage.setVisibility(JavaVisibility.PUBLIC);
        listByPage.setReturnType(new FullyQualifiedJavaType("RestResp<RestData<"
                + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">>"));
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("@Valid @BeanParam PageModel"),"pageModel");
        listByPage.getParameters().add(parameter);
        listByPage.getBodyLines().add(Bean+" "+bean+" = new "+Bean+"();");
        listByPage.getBodyLines().add(bean+".setPageNo(pageModel.getPageNo());");
        listByPage.getBodyLines().add(bean+".setPageSize(pageModel.getPageSize());");
        listByPage.getBodyLines().add("return new RestResp<>("+xService+".listByPage("+bean+"));");
        restClass.getMethods().add(listByPage);

        Method get = new Method("get");
        get.getAnnotations().add("@GET");
        get.getAnnotations().add("@Path(\"{id}\")");
        get.getAnnotations().add("@ApiOperation(\"详情\")");
        get.setVisibility(JavaVisibility.PUBLIC);
        get.setReturnType(new FullyQualifiedJavaType("RestResp<"
                + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") "+pk+""),"id");
        get.getParameters().add(parameter);
        get.getBodyLines().add("return new RestResp<>("+xService+".getByPrimaryKey(id));");
        restClass.getMethods().add(get);

        Method add = new Method("add");
        add.getAnnotations().add("@POST");
        add.getAnnotations().add("@Path(\"add\")");
        add.getAnnotations().add("@ApiOperation(\"新增\")");
        add.setVisibility(JavaVisibility.PUBLIC);
        add.setReturnType(new FullyQualifiedJavaType("RestResp<"
                + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">"));
        parameter = new Parameter(new FullyQualifiedJavaType("@NotNull @FormParam(\"bean\") String"),"bean");
        add.getParameters().add(parameter);

        add.getBodyLines().add(Bean+" "+bean+" = JsonUtils.fromJson(bean, "+Bean+".class);");
        add.getBodyLines().add("BeanValidator.validate("+bean+");");
        add.getBodyLines().add(xService+".saveSelective("+bean+");");
        add.getBodyLines().add("return new RestResp<>("+bean+");");
        restClass.getMethods().add(add);

        Method delete = new Method("delete");
        delete.getAnnotations().add("@DELETE");
        delete.getAnnotations().add("@Path(\"{id}\")");
        delete.getAnnotations().add("@ApiOperation(\"删除\")");
        delete.setVisibility(JavaVisibility.PUBLIC);
        delete.setReturnType(new FullyQualifiedJavaType("RestResp"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") "+pk+""),"id");
        delete.getParameters().add(parameter);
        delete.getBodyLines().add(xService+".deleteByPrimaryKey(id);");
        delete.getBodyLines().add("return new RestResp<>();");
        restClass.getMethods().add(delete);

        Method update = new Method("update");
        update.getAnnotations().add("@PUT");
        update.getAnnotations().add("@Path(\"{id}\")");
        update.getAnnotations().add("@ApiOperation(\"修改\")");
        update.setVisibility(JavaVisibility.PUBLIC);
        update.setReturnType(new FullyQualifiedJavaType("RestResp"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") "+pk+""),"id");
        update.getParameters().add(parameter);
        parameter = new Parameter(new FullyQualifiedJavaType("@NotNull @FormParam(\"bean\") String"),"bean");
        update.getParameters().add(parameter);
        update.getBodyLines().add(Bean+" "+bean+" = JsonUtils.fromJson(bean, "+Bean+".class);");
        update.getBodyLines().add(bean+".setId(id);");
        update.getBodyLines().add("BeanValidator.validate("+bean+");");
        update.getBodyLines().add(xService+".updateByPrimaryKeySelective("+bean+");");
        update.getBodyLines().add("return new RestResp<>();");
        restClass.getMethods().add(update);
    }

}