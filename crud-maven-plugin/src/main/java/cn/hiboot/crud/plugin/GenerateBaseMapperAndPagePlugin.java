package cn.hiboot.crud.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

public class GenerateBaseMapperAndPagePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfaze.getSuperInterfaceTypes().clear();//清除原来的rootInterface
        interfaze.getImportedTypes().clear();

        String rootInterface = this.context.getJavaClientGeneratorConfiguration().getProperty("rootInterface");
        String repository = this.context.getJavaClientGeneratorConfiguration().getProperty("repository");

        if(rootInterface != null){
            String pk = "Object";//default PK Object
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            if(primaryKeyColumns != null && primaryKeyColumns.size() == 1){
                for (IntrospectedColumn keyColumn : primaryKeyColumns) {
                    pk = keyColumn.getFullyQualifiedJavaType().getShortName();
                }
            }

            FullyQualifiedJavaType baseMapper = new FullyQualifiedJavaType("BaseMapper<"
                    + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ","
                    + pk+ ">");
            interfaze.addImportedType(new FullyQualifiedJavaType(rootInterface));
            interfaze.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
            interfaze.addSuperInterface(baseMapper);
        }

        if(repository != null){
            interfaze.addImportedType(new FullyQualifiedJavaType(repository));
            interfaze.getAnnotations().add("@Repository");
        }

        interfaze.getMethods().clear();
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();// 数据库表名
        XmlElement parentElement = document.getRootElement();

        // 添加sql——where
        XmlElement sql = new XmlElement("sql");
        sql.addAttribute(new Attribute("id", "sql_where"));
        XmlElement where = new XmlElement("where");
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            where.addElement(isNotNullElement);

            sb.setLength(0);
            if(index > 0){
                sb.append(" and ");
            }
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            isNotNullElement.addElement(new TextElement(sb.toString()));
            index++;
        }
        sql.addElement(where);
        parentElement.addElement(sql);

        //添加pageSelect
        XmlElement select = new XmlElement("select");
        select.addAttribute(new Attribute("id", "pageSelect"));
        select.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        select.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        select.addElement(new TextElement("select"));
        XmlElement baseColumnList = new XmlElement("include");
        baseColumnList.addAttribute(new Attribute("refid", "Base_Column_List"));
        select.addElement(baseColumnList);
        select.addElement(new TextElement("from " + tableName));
        XmlElement include = new XmlElement("include");
        include.addAttribute(new Attribute("refid", "sql_where"));
        select.addElement(include);
        select.addElement(new TextElement(" ORDER BY update_time DESC LIMIT #{pageNo},#{pageSize}"));
        parentElement.addElement(select);

        //添加pageCount
        XmlElement pageCount = new XmlElement("select");
        pageCount.addAttribute(new Attribute("id", "pageCount"));
        pageCount.addAttribute(new Attribute("resultType", "java.lang.Integer"));
        pageCount.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        pageCount.addElement(new TextElement("select COUNT(*) from " + tableName));
        pageCount.addElement(include);
        parentElement.addElement(pageCount);

        //add selectByCondition
        XmlElement con = new XmlElement("select");
        con.addAttribute(new Attribute("id", "selectByCondition"));
        con.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        con.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        con.addElement(new TextElement("select"));
        con.addElement(baseColumnList);
        con.addElement(new TextElement("from " + tableName));
        con.addElement(include);
        parentElement.addElement(con);

        return true;
    }


}