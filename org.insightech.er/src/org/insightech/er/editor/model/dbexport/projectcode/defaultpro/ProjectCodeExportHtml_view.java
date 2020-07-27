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

/**
 * html table 页面
 * @author ljc
 *
 */
public class ProjectCodeExportHtml_view extends ProjectCodeTemplate{

	private static final String TEMPLATE;

	static {
		try {
			TEMPLATE = ProjectCodeCommon.loadResource("@html_view");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}
	public ProjectCodeExportHtml_view(ProjectCodeSetting setting, ExportJavaSetting exportJavaSetting,
			ERDiagram diagram) {
		super(setting, exportJavaSetting, diagram);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateContent(ERTable table) throws IOException {
		this.importClasseNames.clear();
		this.sets.clear();

		String content = TEMPLATE;
		content = replaceTableInfo(content,table);
		content=content.replaceAll("@javaFolder", this.exportJavaSetting.getJavaFolder());
		String classDescription = ResourceString.getResourceString("java.template.class.description").replaceAll(
				"@LogicalTableName",Matcher.quoteReplacement(table.getLogicalName())).replaceAll("@tableDescription", table.getDescription());
		content=replaceTableSQLInfo(content,table);
		content=this.replaceViewHtml(content, table, ProjectCodeCommon.getCamelCaseName(table));
		content=this.replaceTableColumnInfo(content, table);
		content = this.replaceClassInfo(content, classDescription,ProjectCodeCommon.getCamelCaseName(table),this.exportJavaSetting.getClassNameSuffix());
//		content = this.replaceImportInfo(content);
		content = this.replaceConstructorInfo(content);

		return content;
	}

	@Override
	public String getOutPath(ERTable table) {
		String folderPath=parsePackageToFileFolderPath(this.projectCodeSetting.getViewPath(),File.separator);
		return folderPath+"view"+ProjectCodeCommon.getCamelCaseName(table)+".jsp";
	}

	@Override
	public String getNamespace() {
		StringBuilder sb=new StringBuilder(); 
		String packageName=this.exportJavaSetting.getPackageName();
		String javaFolder=this.projectCodeSetting.getViewPath();
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
