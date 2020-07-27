package org.insightech.er.editor.model.dbexport.java.exportcode;

import java.io.IOException;

import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;

public class ExportToJavaComponent extends ExportToJavaTemplate{

	private   String TEMPLATE;
	private String exportFileName;
	
	public ExportToJavaComponent(ExportJavaSetting exportJavaSetting){
		try {
			TEMPLATE = loadResource("groupcode/@component");
		 setExportJavaSetting(exportJavaSetting);

		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public StringBuilder generateContent(ERDiagram diagram, ERTable table, String compositeIdClassName) throws IOException 
	{
		StringBuilder content=new StringBuilder();
		exportFileName=table.getLogicalName()+"Component";
		
		String template = TEMPLATE;
		if( template!=null && template.length()>0)
		{
			StringBuilder columnSB=new StringBuilder();
			this.addColumnName(columnSB, template, table);
			template=columnSB.toString();
			this.addCompositeIdContent(content,template, compositeIdClassName, table);
		}
		return content;
	}

	@Override
	public String getExportFileName() {
		return this.exportFileName+".java";
		 
	}

	
	 
	
}
