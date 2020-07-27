package org.insightech.er.editor.model.dbexport.projectcode.bebe;



import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import org.insightech.er.ResourceString;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeCommon;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeSetting;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeTemplate;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;
import org.insightech.er.util.Check;

/**
 * 
 * @author ljc
 *
 */
public class ProjectCodeExportBebeDaoImpl extends ProjectCodeTemplate{

	private static final String DAOTEMPLATE;

	private static final String DAO_PRIMARYKEY;
 
	
	static {
		try {
			DAOTEMPLATE = ProjectCodeCommon.loadResource("bebe/@daoImpl");
			DAO_PRIMARYKEY = ProjectCodeCommon.loadResource("bebe/@daoImpl_primarykey");

		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public ProjectCodeExportBebeDaoImpl(ProjectCodeSetting setting, ExportJavaSetting exportJavaSetting, ERDiagram diagram) {
		super(setting,exportJavaSetting, diagram);
	}

	@Override
	public String generateContent(ERTable table) throws IOException {
		this.importClasseNames.clear();
//		this.importClasseNames.add(getClassName(table));
		this.sets.clear();

		String content = DAOTEMPLATE;
		
		content = replaceTableInfo(content,table);

		String classDescription = ResourceString.getResourceString("java.template.class.description").replaceAll(
				"@LogicalTableName",Matcher.quoteReplacement(table.getLogicalName())).replaceAll("@tableDescription", table.getDescription());
		content = replacePrimarykeyTemplate(content,table,classDescription);
		content = this.replaceTableSQL(content,table);
		content = this.replaceClassInfo(content, classDescription,ProjectCodeCommon.getCamelCaseName(table),this.exportJavaSetting.getClassNameSuffix());
		content = this.replaceImportInfo(content);
		content = this.replaceConstructorInfo(content);

		return content;
	}

	@Override
	public String getOutPath(ERTable table) {
		return "bebe"+File.separator+this.getClassName(table)+"DaoImpl.java";
	}
	
	private String replacePrimarykeyTemplate(String content, ERTable table,String classDescription) {
		
		if(table.getPrimaryKeySize()>0)
		{
			
			content = content.replace("@daoImpl_primarykey", DAO_PRIMARYKEY);
		
			content = this.replaceClassInfo(content, classDescription,ProjectCodeCommon.getCamelCaseName(table),this.exportJavaSetting.getClassNameSuffix());
		}else{
			content = content.replace("@daoImpl_primarykey", "");
		}
		return content;
	}
	
	private String replaceTableSQL(String template,ERTable table)
	{
		List<NormalColumn> columns=table.getNormalColumns();
		StringBuilder insertColumnsName=new StringBuilder();
		StringBuilder insertColumnsPlaceHolder=new StringBuilder();
		StringBuilder insertObjectValue=new StringBuilder();
		StringBuilder updateColumnsList=new StringBuilder();
		StringBuilder updateObjectValue=new StringBuilder();
		String className=this.getClassName(table);
		String objectParamName=ProjectCodeCommon.getCamelCaseName(className,false,false);
		String primaryKeyName="";
		String primaryKeyValue="";
		String primaryKeyType="";
		for (int i=0,k=columns.size();i<k;i++) {
			NormalColumn normalColumn =columns.get(i);
			if(i>0)
			{
				insertColumnsName.append(",");
				insertColumnsPlaceHolder.append(",");
				insertObjectValue.append(",");
				
			}
			String logicalName=normalColumn.getPhysicalName();
			insertColumnsName.append(logicalName);
			insertColumnsPlaceHolder.append("?");
			insertObjectValue.append(objectParamName).append(".get").append(ProjectCodeCommon.getCamelCaseName(logicalName,true,false)).append("()");
			
			if(normalColumn.isPrimaryKey())
			{
				primaryKeyName=logicalName;
				primaryKeyValue=objectParamName+".get"+ProjectCodeCommon.getCamelCaseName(logicalName,true,false)+"()";
				primaryKeyType=this.getClassName(normalColumn.getType());
			}else{
				if(updateColumnsList.length()>0)
				{
					updateColumnsList.append(",");
					updateObjectValue.append(",");
				}
				updateColumnsList.append(logicalName).append("=?");
				updateObjectValue.append(objectParamName).append(".").append("get").append(ProjectCodeCommon.getCamelCaseName(logicalName,true,false)).append("()");
			}
			
		}
		String value = template;
		value = value.replaceAll("@insertColumnsName", insertColumnsName.toString());
		value = value.replaceAll("@insertColumnsPlaceHolder", insertColumnsPlaceHolder.toString());
		value = value.replaceAll("@insertObjectValue", insertObjectValue.toString());
		
		value = value.replaceAll("@columnsNameList", insertColumnsName.toString());
		
		value = value.replaceAll("@primaryKeyName", primaryKeyName);
		value = value.replaceAll("@PrimaryKeyName", ProjectCodeCommon.getCamelCaseName(primaryKeyName,true,false));
		value = value.replaceAll("@primaryKeyType", primaryKeyType);
		value = value.replaceAll("@primaryKeyValue", primaryKeyValue);
		value = value.replaceAll("@updateColumnsList", updateColumnsList.toString());
		value = value.replaceAll("@updateObjectValue", updateObjectValue.toString());
		
		value = value.replaceAll("@tableName", table.getPhysicalName());
		return value;
	}
	
	@Override
	public String getNamespace()
	{
		StringBuilder sb=new StringBuilder(); 
		String packageName=this.exportJavaSetting.getPackageName();
		String javaFolder=null;//this.projectCodeSetting.getServiceImplPath();
		if(!Check.isEmptyTrim(packageName))
		{
			sb.append(packageName);
		}
		if(!Check.isEmptyTrim(javaFolder))
		{
			if(sb.length()>0)
			{
				sb.append(".");
			}
			sb.append(javaFolder);
		}
		return sb.toString();
	}

}

