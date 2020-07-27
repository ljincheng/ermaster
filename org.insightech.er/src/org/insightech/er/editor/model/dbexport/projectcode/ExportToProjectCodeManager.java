package org.insightech.er.editor.model.dbexport.projectcode;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.dbexport.projectcode.bebe.ProjectCodeExportBebeComponent;
import org.insightech.er.editor.model.dbexport.projectcode.bebe.ProjectCodeExportBebeDao;
import org.insightech.er.editor.model.dbexport.projectcode.bebe.ProjectCodeExportBebeDaoImpl;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportComponent;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportComponentImpl;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportController;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportDefaultDao;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportHtml_add;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportHtml_list;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportHtml_list_table;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportHtml_view;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportMybatisXml;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportService;
import org.insightech.er.editor.model.dbexport.projectcode.defaultpro.ProjectCodeExportServiceImpl;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;

public class ExportToProjectCodeManager {
	
	private ExportJavaSetting exportJavaSetting;

	protected ERDiagram diagram;


 
	
	public ExportToProjectCodeManager(ExportJavaSetting exportJavaSetting,ERDiagram diagram) {
		 
		this.exportJavaSetting = exportJavaSetting;
		this.diagram = diagram;
	 
	}
	
	
	
	public void doProcess() throws IOException, InterruptedException {
		
		String javaFolder=exportJavaSetting.getJavaFolder();
		ProjectCodeSetting setting=new ProjectCodeSetting();
		setting.setBeanPath("model."+javaFolder);
		setting.setDaoPath("dao."+javaFolder);
		setting.setMybatisXmlPath("mapping"+File.separator+javaFolder);
		setting.setComponentPath("component."+javaFolder);
		setting.setComponentImplPath("component."+javaFolder+".impl");
		setting.setServicePath("service."+javaFolder);
		setting.setServiceImplPath("service."+javaFolder+".impl");
		setting.setControllerPath("controller."+javaFolder);
		setting.setViewPath("view"+File.separator+javaFolder);
		
		ProjectCodeTemplate[] templates={new ExportToProjectCode_PojoFile(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportDefaultDao(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportMybatisXml(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportComponent(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportComponentImpl(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportService(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportServiceImpl(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportController(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportHtml_list(setting,exportJavaSetting, diagram)
				,new ProjectCodeExportHtml_list_table(setting, exportJavaSetting, diagram)
				,new ProjectCodeExportHtml_add(setting, exportJavaSetting, diagram)
				,new ProjectCodeExportHtml_view(setting, exportJavaSetting, diagram)
//				,new ProjectCodeExportBebeDao(setting,exportJavaSetting, diagram)
//				,new ProjectCodeExportBebeDaoImpl(setting,exportJavaSetting, diagram)
//				,new ProjectCodeExportBebeComponent(setting,exportJavaSetting, diagram)
		};
		for (ERTable table : diagram.getDiagramContents().getContents().getTableSet().getList()) {
			this.doPreTask(table);
			
			for(ProjectCodeTemplate template:templates)
			{
				template.writeOut(table);
			}

		}
	}
	
	protected void doPreTask(ERTable table) {
	}

	protected void doPostTask() throws InterruptedException {
	}

}
