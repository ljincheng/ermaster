package org.insightech.er.editor.model.dbexport.projectcode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.insightech.er.db.sqltype.SqlType;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;
import org.insightech.er.util.Check;
import org.insightech.er.util.io.FileUtils;

public abstract class ProjectCodeTemplate {

	protected ExportJavaSetting exportJavaSetting;
	protected ProjectCodeSetting projectCodeSetting;
	protected ERDiagram diagram;

	protected String packageDir;

	protected Set<String> importClasseNames;

	protected Set<String> sets;

	public ProjectCodeTemplate(ProjectCodeSetting setting,ExportJavaSetting exportJavaSetting,ERDiagram diagram) {
		this.packageDir = exportJavaSetting.getPackageName().replaceAll("\\.",
				File.separator);
		this.projectCodeSetting=setting;
		
		this.exportJavaSetting = exportJavaSetting;
		this.diagram = diagram;

		this.importClasseNames = new TreeSet<String>();
		this.sets = new TreeSet<String>();
	}
	
	/**
	 * 生成内容
	 * @param diagram
	 * @param table
	 * @return
	 * @throws IOException
	 */
	public abstract String generateContent( ERTable table) throws IOException;
	
	/**
	 * 文件路径
	 * @return
	 */
	public abstract String getOutPath(ERTable table);
	
	/**
	 * 生成文件
	 * @param diagram
	 * @param table
	 * @throws IOException
	 */
	public void writeOut(ERTable table)throws IOException
	{
		String content=generateContent(table);
		String dstPath=getOutPath(table);
		writeOut(dstPath,content);
	}
	 
	
	protected void writeOut(String dstPath,String content) throws IOException {
		 dstPath = this.exportJavaSetting.getJavaOutput() + File.separator
				+ "src" + File.separator+dstPath;
		File file = new File(dstPath);
		file.getParentFile().mkdirs();
		FileUtils.writeStringToFile(file, content,this.exportJavaSetting.getSrcFileEncoding());
	}

	
	protected   String getClassName(ERTable table) {
		return  ProjectCodeCommon.getClassName(table);
	}


	protected String getClassName(SqlType type) {
		if (type == null) {
			return "";
		}
		Class clazz = type.getJavaClass();

		String name = clazz.getCanonicalName();
		if (!name.startsWith("java.lang")) {
			this.importClasseNames.add(name);
		}

		return clazz.getSimpleName();
	}
	
	public abstract String getNamespace();
	
	protected String replaceClassInfo(String content, String classDescription,
			String className, String classNameSufix) {
		String namespace=this.getNamespace();
		if (Check.isEmptyTrim(namespace)) {
			content = content.replaceAll("package @package;\r\n\r\n", "");
			content = content.replaceAll("package @package;\n\n", "");//linux
			content = content.replaceAll("@package", "");//清除全部
			content = content.replaceAll("@namespace", "");//清除全部
		} else {
			content = content.replaceAll("@package",namespace);
			content = content.replaceAll("@namespace",namespace+".");
		}
		String logicalTableName=ProjectCodeCommon.getCamelCaseName(className,false,false);
		content = content.replaceAll("@classDescription", classDescription);
		content = content.replaceAll("@PhysicalTableName", className);
		content = content.replaceAll("@physicalTableName", logicalTableName);
		content = content.replaceAll("@LogicalTableName", className);
		content = content.replaceAll("@logicalTableName", logicalTableName);
		content = content.replaceAll("@suffix",ProjectCodeCommon.getCamelCaseName(classNameSufix, true));
//		content = content.replaceAll("@version", "@version \\$Id\\$");
		content = content.replaceAll("@version", "@version ");

		return content;
	}
	
	private String getJavaObjectName(String columnName,Boolean capital)
	{
		return ProjectCodeCommon.getCamelCaseName(columnName,capital,false);
	}
	
	protected String replaceTableSQLInfo(String content, ERTable table) {
		 
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder cols=new StringBuilder();
		StringBuilder placeholder=new StringBuilder();
//		StringBuilder valParams=new StringBuilder();
		StringBuilder updateColumn=new StringBuilder();
		StringBuilder whereColumnMap=new StringBuilder();
		StringBuilder insertColumnList=new StringBuilder();
		StringBuilder insertColumnValue=new StringBuilder();
		String primaryKeyName="";
		String primaryKeyValue="";
		String primaryKeyType="";
		String insertKeyProperty="";
		for (int i=0,k=columns.size();i<k;i++) {
			NormalColumn normalColumn =columns.get(i);
			if(i>0)
			{
				cols.append(",");
				placeholder.append(",");
//				valParams.append(",");
				
			}
			String logicalName=normalColumn.getPhysicalName();
			String logicalJavaObjectName=getJavaObjectName(logicalName,false);
			cols.append(logicalName);
			placeholder.append("#{").append(logicalJavaObjectName).append("}");
			whereColumnMap.append("        <if test=\"")
			.append(logicalJavaObjectName).append("!= null ");

			if(normalColumn.getType()!=null && ("java.lang.String").equals(normalColumn.getType().getJavaClass().getName()))
			{
				whereColumnMap.append("and ").append(logicalJavaObjectName).append(" != '' ");
			}
			
			whereColumnMap.append("\"> and ")
			.append(logicalName).append("=#{").append(logicalJavaObjectName).append("}</if>\r\n");
			
			if(logicalName!=null && "createtime".equals(logicalName.toLowerCase()))
			{
				whereColumnMap.append("        <if test=\"startDate  != null \">")
				.append("<![CDATA[ and ").append(logicalName).append("  >= #{startDate}]]>")
				.append("</if>\r\n        <if test=\"endDate  != null  \">")
				.append("<![CDATA[ and  ").append(logicalName).append(" <= #{endDate}]]>")
				.append("</if>\r\n");
			}
			
			if(normalColumn.isPrimaryKey())
			{
				primaryKeyName=logicalName;
				primaryKeyType=this.getClassName(normalColumn.getType());
			}else{
				if(updateColumn.length()>0)
				{
					updateColumn.append(",");
				}
				updateColumn.append(logicalName).append("= #{").append(logicalJavaObjectName).append("}");
			}
			if(normalColumn.isPrimaryKey() && normalColumn.isAutoIncrement())
			{
				insertKeyProperty=" keyProperty=\""+logicalJavaObjectName+"\"  useGeneratedKeys=\"true\"";
			}else{
				if(insertColumnList.length()>0)
				{
					insertColumnList.append(",");
					insertColumnValue.append(",");
				}
				insertColumnList.append(logicalName);
				insertColumnValue.append("#{").append(logicalJavaObjectName).append("}");
			}
			
			
		}
		String value = content;
//		System.out.println(cols.toString());
		value = value.replaceAll("@columnsNameList", cols.toString());
		value = value.replaceAll("@insertColumnList", insertColumnList.toString());
		value = value.replaceAll("@insertColumnValue", insertColumnValue.toString());
		value = value.replaceAll("@placeholder", placeholder.toString());
//		System.out.println(placeholder.toString());
		value = value.replaceAll("@whereColumnMap", whereColumnMap.toString());
		value = value.replaceAll("@insertKeyProperty",insertKeyProperty );
//		System.out.println(primaryKeyName.toString());
		value = value.replaceAll("@primaryKeyName", ProjectCodeCommon.getCamelCaseName(primaryKeyName,false,false));
		value = value.replaceAll("@primaryKeyColumnName", primaryKeyName);
		value = value.replaceAll("@PrimaryKeyName", ProjectCodeCommon.getCamelCaseName(primaryKeyName,true,false));
		value = value.replaceAll("@primaryKeyValue", primaryKeyValue);
		value = value.replaceAll("@updateColumns", updateColumn.toString());
		value = value.replaceAll("@tableName", table.getPhysicalName());
		value = value.replaceAll("@primaryKeyType", primaryKeyType);
		return value;
	}
	
	protected String replaceTableColumnInfo(String content, ERTable table) {
		 
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder tableHeaderColumn=new StringBuilder();
		StringBuilder tableDataColumn=new StringBuilder();
		 int columnsTotal=columns.size();
		for (int i=0;i<columnsTotal;i++) {
			NormalColumn normalColumn =columns.get(i);
			String logicalName=normalColumn.getPhysicalName();
			String logicalJavaObjectName=getJavaObjectName(logicalName,false);
			tableHeaderColumn.append("<th>").append(normalColumn.getLogicalName()).append("</th>\r\n");
			if(normalColumn.getType()!=null && ("java.util.Date").equals(normalColumn.getType().getJavaClass().getName()))
			{
				tableDataColumn.append("<td ><f:formatDate value=\"\\$\\{modelObj.").append(logicalJavaObjectName).append("\\}\"  pattern=\"yyyy-MM-dd\"/></td>\r\n");
			}else{
				tableDataColumn.append("<td ><c:out value=\"\\$\\{modelObj.").append(logicalJavaObjectName).append("\\}\"/></td>\r\n");
			}
		}
		String value = content;
//		System.out.println(cols.toString());
		value = value.replaceAll("@tableHeaderColumn", tableHeaderColumn.toString());
		value = value.replaceAll("@tableDataColumn", tableDataColumn.toString());
		value = value.replaceAll("@tableColumnTotal\\+1", String.valueOf((columnsTotal+1)));
		value = value.replaceAll("@tableColumnTotal", String.valueOf(columnsTotal));
		return value;
	}
	
	protected String replaceAddFormHtml(String content,ERTable table,String className)
	{
		String logicalTableName=ProjectCodeCommon.getCamelCaseName(className,false,false);
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder formHtml=new StringBuilder();
		 int columnsTotal=columns.size();
		 String primaryKeyName=null;
		for (int i=0;i<columnsTotal;i++) {
			NormalColumn normalColumn =columns.get(i);
			String logicalName=normalColumn.getPhysicalName();
			String logicalJavaObjectName=getJavaObjectName(logicalName,false);
			if(normalColumn.isPrimaryKey())
			{
				primaryKeyName=logicalJavaObjectName;
			}else {
				if(normalColumn.getType()!=null && ("java.util.Date").equals(normalColumn.getType().getJavaClass().getName()))
				{
					formHtml.append("<div class=\"form-group  form-inline\">\r\n<label for=\"").append(logicalJavaObjectName).append("\" class=\"monospaced_lg\">").append(normalColumn.getLogicalName()).append(":</label>\r\n");
					formHtml.append("<input type=\"text\" id=\"").append(logicalJavaObjectName).append("\" name=\"").append(logicalJavaObjectName).append("\" class=\"form-control w400\" ") ;//value=\"<c:out value=\"\\$\\{").append(logicalTableName).append(".").append(logicalJavaObjectName).append("\\}\"/>\" />\r\n</div> \r\n");
					formHtml.append("onclick=\"WdatePicker({dateFmt:'yyyy-MM-dd',readOnly:'readOnly'})\" value=\"<f:formatDate value=\"\\$\\{").append(logicalTableName).append(".").append(logicalJavaObjectName).append("\\}\"  pattern=\"yyyy-MM-dd\"/>\" ").append(" />\r\n</div> \r\n");;
				}else {
				formHtml.append("<div class=\"form-group  form-inline\">\r\n<label for=\"").append(logicalJavaObjectName).append("\" class=\"monospaced_lg\">").append(normalColumn.getLogicalName()).append(":</label>\r\n");
				formHtml.append("<input type=\"text\" id=\"").append(logicalJavaObjectName).append("\" name=\"").append(logicalJavaObjectName).append("\" class=\"form-control w400\" value=\"<c:out value=\"\\$\\{").append(logicalTableName).append(".").append(logicalJavaObjectName).append("\\}\"/>\" />\r\n</div> \r\n");
				}
			}
			
		}
		// 续上按钮
		formHtml.append(" <div class=\"form-group\">\r\n<div class=\"col-sm-offset-2 col-sm-10\">\r\n");
		if(primaryKeyName!=null)
		{
			formHtml.append("<c:if test=\"\\$\\{empty ").append(logicalTableName).append(".").append(primaryKeyName).append("\\}\" >\r\n")
		.append("<input type=\"button\" onclick=\'submitFn(\"add").append(className).append(".do\")\' class=\"btn btn-primary\" id=\"addBtn\" value=\"保存\" />\r\n")
		.append("</c:if>\r\n")
		.append("<c:if test=\"\\$\\{not empty ").append(logicalTableName).append(".").append(primaryKeyName).append("\\}\" >\r\n")
		.append("<input type=\"hidden\" id=\"").append(primaryKeyName).append("\" name=\"").append(primaryKeyName).append("\" value=\"<c:out value=\"\\$\\{").append(logicalTableName).append(".").append(primaryKeyName).append("\\}\"/>\" />\r\n")
		.append("<input type=\"button\" onclick=\'submitFn(\"edit").append(className).append(".do\")\' class=\"btn btn-primary\" id=\"modifyBtn\" value=\"保存\" />\r\n")
		.append("</c:if>\r\n");
		}else {
			formHtml.append("<input type=\"button\" onclick=\'submitFn(\"add").append(className).append(".do\")\' class=\"btn btn-primary\" id=\"addBtn\" value=\"新增\" />\r\n")
			.append("<input type=\"button\" onclick=\'submitFn(\"edit").append(className).append(".do\")\' class=\"btn btn-primary\" id=\"modifyBtn\" value=\"修改\" />\r\n");
		}
		formHtml.append("</div>");	
 
		String value = content;
//		System.out.println(cols.toString());
		value = value.replaceAll("@formColumn", formHtml.toString());
	 
		return value;
	}
	
	protected String replaceViewHtml(String content,ERTable table,String className)
	{
		String logicalTableName=ProjectCodeCommon.getCamelCaseName(className,false,false);
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder formHtml=new StringBuilder("<table class=\"table table-bordered \">");
		 int columnsTotal=columns.size();
		for (int i=0;i<columnsTotal;i++) {
			NormalColumn normalColumn =columns.get(i);
			String logicalName=normalColumn.getPhysicalName();
			String logicalJavaObjectName=getJavaObjectName(logicalName,false);
			formHtml.append("<tr>\r\n<td>").append(normalColumn.getLogicalName()).append("</td>\r\n");
			if(normalColumn.getType()!=null && ("java.util.Date").equals(normalColumn.getType().getJavaClass().getName()))
			{
				formHtml.append("<th ><f:formatDate value=\"\\$\\{").append(logicalTableName).append(".").append(logicalJavaObjectName).append("\\}\"  pattern=\"yyyy-MM-dd\"/></t>\r\n</tr>\r\n");
			}else{
				formHtml.append("<th><c:out value=\"\\$\\{").append(logicalTableName).append(".").append(logicalJavaObjectName).append("\\}\"/></th>\r\n</tr>\r\n");
			}
			
		}
		// 续上按钮
		formHtml.append("</table>\r\n");
		 
 
		String value = content;
//		System.out.println(cols.toString());
		value = value.replaceAll("@viewTable", formHtml.toString());
	 
		return value;
	}
	
	protected String replaceImportInfo(String content) {
		StringBuilder imports = new StringBuilder();
		for (String importClasseName : this.importClasseNames) {
			imports.append("import ");
			imports.append(importClasseName);
			imports.append(";\r\n");
		}

		content = content.replaceAll("@import\r\n",Matcher.quoteReplacement(imports.toString()));
		content = content.replaceAll("@import\n",Matcher.quoteReplacement(imports.toString()));

		return content;
	}
	
	protected String replaceConstructorInfo(String content) {
		StringBuilder constructor = new StringBuilder();
		for (String tableName : this.sets) {
			constructor.append("\t\tthis.");
			constructor.append(ProjectCodeCommon.getCamelCaseName(tableName, false));
			constructor.append("Set = new HashSet<");
			constructor.append(ProjectCodeCommon.getCamelCaseName(tableName, true)
					+ ProjectCodeCommon.getCamelCaseName(
							this.exportJavaSetting.getClassNameSuffix(), true));
			constructor.append(">();\r\n");
		}

		content = content.replaceAll("@constructor\r\n",
				Matcher.quoteReplacement(constructor.toString()));

		return content;
	}

	protected String replaceTableInfo(String value, ERTable table) {
		String className=ProjectCodeCommon.getCamelCaseName(table);
		String classNameParam=ProjectCodeCommon.getCamelCaseName(table.getLogicalName(),false,false);
		String PhysicalTableName=ProjectCodeCommon.getCamelCaseName(table.getPhysicalName(),true,false);
		String physicalTableName=ProjectCodeCommon.getCamelCaseName(table.getPhysicalName(),false,true);
		String LogicalTableName=className;
		String logicalTableName=classNameParam;
//		String value = template;

		value = value.replaceAll("@classDescription", table.getDescription());
		value = value.replaceAll("@PhysicalTableName", PhysicalTableName);
		value = value.replaceAll("@physicalTableName", physicalTableName);
		value = value.replaceAll("@LogicalTableName", LogicalTableName);
		value = value.replaceAll("@logicalTableName", logicalTableName);
		return value;
	}
	
	/**
	 * 把包路径转换为文件目录路径
	 * @param path
	 * @param endStrWhenNoEmpty
	 * 当path非空字符串时在返回值中添加字符串
	 * @return
	 */
	protected String parsePackageToFileFolderPath(String path,String endStrWhenNoEmpty)
	{
		if(path!=null && !path.trim().isEmpty())
		{
			String filePath=path.replaceAll("\\.", "\\"+File.separator);
			if(endStrWhenNoEmpty!=null)
			{
				filePath=filePath+endStrWhenNoEmpty;
			}
			return filePath;
		}
		return "";
	}
	
	protected String buildPackageName(String projectPackagePath,String javaPackagePath,String className)
	{
		String result="";
		if(projectPackagePath!=null && !projectPackagePath.trim().isEmpty())
		{
			result=projectPackagePath+".";
		}
		if(javaPackagePath!=null && !javaPackagePath.trim().isEmpty())
		{
			result+=javaPackagePath+".";
		}
		result+=className;
		
		return result;
	}
}
