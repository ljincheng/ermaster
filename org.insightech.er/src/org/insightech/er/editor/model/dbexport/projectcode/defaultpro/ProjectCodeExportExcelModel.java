package org.insightech.er.editor.model.dbexport.projectcode.defaultpro;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import org.insightech.er.ResourceString;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeCommon;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeSetting;
import org.insightech.er.editor.model.dbexport.projectcode.ProjectCodeTemplate;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;
import org.insightech.er.util.Check;

public class ProjectCodeExportExcelModel extends ProjectCodeTemplate{

	private static final String DAOTEMPLATE;

 
	
	static {
		try {
			DAOTEMPLATE = ProjectCodeCommon.loadResource("@excel_model");

		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public ProjectCodeExportExcelModel(ProjectCodeSetting setting, ExportJavaSetting exportJavaSetting, ERDiagram diagram) {
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
		content = replacePrimarykeyInfo(content,table,classDescription);
		content=replaceTableSQLInfo(content,table);
		content = this.replaceClassInfo(content, classDescription,ProjectCodeCommon.getCamelCaseName(table),this.exportJavaSetting.getClassNameSuffix());
		
		this.importClasseNames.add(this.buildPackageName(exportJavaSetting.getPackageName(), this.projectCodeSetting.getBeanPath(), getClassName(table)+this.exportJavaSetting.getClassNameSuffix()));
		content = this.replaceImportInfo(content);
		content = this.replaceConstructorInfo(content);

		return content;
	}

	@Override
	public String getOutPath(ERTable table) {
		String folderPath=parsePackageToFileFolderPath(this.getNamespace(),File.separator);
		return folderPath+this.getClassName(table)+"ExcelModel.java";
	}
	
	private String replacePrimarykeyInfo(String content, ERTable table,String classDescription) {
		
		if(table.getPrimaryKeySize()>0)
		{
			
			content = content.replace("@dao_primarykey", DAO_PRIMARYKEY);
//		
//			String PrimaryKeyName="";
//			String primaryKeyName="";
//			String primaryKeyType="";
//			for(NormalColumn column: table.getPrimaryKeys())
//			{
//				PrimaryKeyName=ProjectCodeCommon.getCamelCaseName(column.getLogicalName(),true,true);
//				primaryKeyName=ProjectCodeCommon.getCamelCaseName(PrimaryKeyName,false,true);
//				primaryKeyType=this.getClassName(column.getType());
//			}
//		
// 
//
//		content = content.replaceAll("@PrimaryKeyName", PrimaryKeyName);
//		content = content.replaceAll("@primaryKeyName", primaryKeyName);
//		content = content.replaceAll("@primaryKeyType", primaryKeyType);
	 
//		content = this.replaceClassInfo(content, classDescription,ProjectCodeCommon.getCamelCaseName(table),this.exportJavaSetting.getClassNameSuffix());
		}else{
			content = content.replace("@dao_primarykey", "");
		}
		return content;
	}
	
	@Override
	public String getNamespace()
	{
		StringBuilder sb=new StringBuilder(); 
		String packageName=this.exportJavaSetting.getPackageName();
		String beanPath=this.projectCodeSetting.getBeanPath();
		if(!Check.isEmptyTrim(packageName))
		{
			sb.append(packageName);
		}
		if(!Check.isEmptyTrim(beanPath))
		{
			if(sb.length()>0)
			{
				sb.append(".");
			}
			sb.append(beanPath);
		}
		return sb.toString();
	}

	 
}

