package org.insightech.er.editor.model.dbexport.java.exportcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.insightech.er.ResourceString;
import org.insightech.er.db.sqltype.SqlType;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.NodeElement;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.TableView;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;
import org.insightech.er.util.io.FileUtils;
import org.insightech.er.util.io.IOUtils;

public abstract class ExportToJavaTemplate {

	protected static final String[] KEYWORDS = { "java.template.constructor",
			"java.template.getter.description",
			"java.template.set.adder.description",
			"java.template.set.getter.description",
			"java.template.set.property.description",
			"java.template.set.setter.description",
			"java.template.setter.description", };
	
	private static final String TEMPLATE_DIR = "javasource/";
	
	protected Set<String> importClasseNames;
	protected Set<String> sets;
	protected ExportJavaSetting exportJavaSetting;
	
	protected static String loadResource(String templateName) throws IOException {
		String resourceName = TEMPLATE_DIR + templateName + ".txt";

		InputStream in = ExportToJavaTemplate.class.getClassLoader()
				.getResourceAsStream(resourceName);

		if (in == null) {
			throw new FileNotFoundException(resourceName);
		}

		try {
			String content = IOUtils.toString(in);

			for (String keyword : KEYWORDS) {
				content = content.replaceAll(keyword, Matcher
						.quoteReplacement(ResourceString
								.getResourceString(keyword)));
			}

			return content;

		} finally {
			in.close();
		}
	}
	
	/**
	 * 生成文件内容
	 * @param diagram
	 * @param table
	 * @param compositeIdClassName
	 * @return
	 * @throws IOException
	 */
	public abstract StringBuilder generateContent(ERDiagram diagram, ERTable table, String compositeIdClassName) throws IOException;
	 
	public abstract String getExportFileName();

	public void setExportJavaSetting(ExportJavaSetting exportJavaSetting)
	{
		this.exportJavaSetting=exportJavaSetting;
	}
	
	
	public void writeOut(String content) throws IOException {
		String dstPath = this.exportJavaSetting.getJavaOutput() + File.separator
				+ "src" +  File.separator +  getExportFileName();
		File file = new File(dstPath);
		file.getParentFile().mkdirs();

		FileUtils.writeStringToFile(file, content,
				this.exportJavaSetting.getSrcFileEncoding());
	}
	
	protected void addColumnName(StringBuilder contents,String template,ERTable table)
	{
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder cols=new StringBuilder();
		StringBuilder placeholder=new StringBuilder();
		StringBuilder valParams=new StringBuilder();
		StringBuilder updateColumn=new StringBuilder();
		StringBuilder updateClassValue=new StringBuilder();
		String className=getPojoClassValueName(table);
		String primaryKeyName="";
		String primaryKeyValue="";
		String primaryKeyType="";
		for (int i=0,k=columns.size();i<k;i++) {
			NormalColumn normalColumn =columns.get(i);
			if(i>0)
			{
				cols.append(",");
				placeholder.append(",");
				valParams.append(",");
				
			}
			String logicalName=normalColumn.getPhysicalName();
			cols.append(logicalName);
			placeholder.append("?");
			valParams.append(className).append(".get").append(getCamelCaseName(logicalName,true,false)).append("()");
			
			if(normalColumn.isPrimaryKey())
			{
				primaryKeyName=logicalName;
				primaryKeyValue=className+".get"+getCamelCaseName(logicalName,true,false)+"()";
				primaryKeyType=this.getClassName(normalColumn.getType());
			}else{
				if(updateColumn.length()>0)
				{
					updateColumn.append(",");
					updateClassValue.append(",");
				}
				updateColumn.append(logicalName).append("=?");
				updateClassValue.append(className).append(".").append("get").append(getCamelCaseName(logicalName,true,false)).append("()");
			}
			
		}
		String value = template;
		System.out.println(cols.toString());
		value = value.replaceAll("@columnsNameSet", cols.toString());
		value = value.replaceAll("@placeholder", placeholder.toString());
		System.out.println(placeholder.toString());
		value = value.replaceAll("@classParams", valParams.toString());
		System.out.println(primaryKeyName.toString());
		value = value.replaceAll("@primaryKeyName", primaryKeyName);
		value = value.replaceAll("@PrimaryKeyName", getCamelCaseName(primaryKeyName,true,false));
		value = value.replaceAll("@primaryKeyValue", primaryKeyValue);
		value = value.replaceAll("@updateColumns", updateColumn.toString());
		value = value.replaceAll("@updateClassValue", updateClassValue.toString());
		value = value.replaceAll("@tableName", table.getPhysicalName());
		value = value.replaceAll("@primaryKeyType", primaryKeyType);
		contents.append(value);
		contents.append("\r\n");
	}
	
	protected void addCompositeIdContent(StringBuilder contents, String template,
			String compositeIdClassName, ERTable table) {
		System.out.println("addCompositeIdContent:");
		System.out.println("compositeIdClassName:"+compositeIdClassName);
		
		String pojoClassName=getPojoClassName(table);
		String pojoClassValueName=getPojoClassValueName(table);
		String compositeIdPropertyName ="";
		if(compositeIdClassName!=null)
		{
			 compositeIdPropertyName = compositeIdClassName.substring(0, 1)
						.toLowerCase() + compositeIdClassName.substring(1);
				System.out.println("compositeIdPropertyName:"+compositeIdPropertyName);
		}
		 
		
		String propertyDescription = ResourceString.getResourceString(
				"java.template.composite.id.property.description").replaceAll(
				"@LogicalTableName",
				Matcher.quoteReplacement(table.getLogicalName()));
		System.out.println("propertyDescription:"+propertyDescription);
		String value = template;

		value = value.replaceAll("@type", compositeIdClassName);
		value = value.replaceAll("@logicalColumnName", propertyDescription);
		value = value.replaceAll("@classDescription", table.getDescription());

		value = value
				.replaceAll("@physicalColumnName", compositeIdPropertyName);
		value = value.replaceAll("@PhysicalColumnName", compositeIdClassName);
		value = value.replaceAll("@PhysicalTableName", getCamelCaseName(table.getPhysicalName(),true,false));
		value = value.replaceAll("@physicalTableName", getCamelCaseName(table.getPhysicalName(),false,true));
		value = value.replaceAll("@LogicalTableName", pojoClassName);
		value = value.replaceAll("@logicalTableName", pojoClassValueName);

		contents.append(value);
		contents.append("\r\n");
	}
	
	protected String getPojoClassName(ERTable table)
	{
		return getCamelCaseName(table.getLogicalName(),true,false);
	}
	protected String getPojoClassValueName(ERTable table)
	{
		return getCamelCaseName(table.getLogicalName(),false,true);
	}
	
	protected String getCamelCaseName(String name,boolean capital,boolean toUpperCase)
	{
		String className = name.toLowerCase();
		if(!toUpperCase)
		{
			className=name;
		}

		if (capital && className.length() > 0) {
			String first = className.substring(0, 1);
			String other = className.substring(1);

			className = first.toUpperCase() + other;
		}

		while (className.indexOf("_") == 0) {
			className = className.substring(1);
		}

		int index = className.indexOf("_");

		while (index != -1) {
			String before = className.substring(0, index);
			if (className.length() == index + 1) {
				className = before;
				break;
			}

			String target = className.substring(index + 1, index + 2);

			String after = null;

			if (className.length() == index + 1) {
				after = "";

			} else {
				after = className.substring(index + 2);
			}

			className = before + target.toUpperCase() + after;

			index = className.indexOf("_");
		}

		return className;
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
}
