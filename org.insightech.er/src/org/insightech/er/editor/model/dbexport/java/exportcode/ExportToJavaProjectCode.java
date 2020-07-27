package org.insightech.er.editor.model.dbexport.java.exportcode;

import java.io.IOException;

import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;

/**
 * 
 * @author ljc
 *
 */
public class ExportToJavaProjectCode {
	
	
	public static void generateContent(ERDiagram diagram, ERTable table, String compositeIdClassName,ExportJavaSetting exportJavaSetting) throws IOException, InterruptedException
	{
		ExportToJavaTemplate componentExp=new ExportToJavaComponent(exportJavaSetting);
		StringBuilder contents= componentExp.generateContent(diagram, table, compositeIdClassName);
		componentExp.writeOut(contents.toString());
		ExportToJavaDao dao=new ExportToJavaDao(exportJavaSetting);
		contents=dao.generateContent(diagram, table, compositeIdClassName);
		dao.writeOut(contents.toString());
		ExportToJavaDaoImpl daoimpl=new ExportToJavaDaoImpl(exportJavaSetting);
		contents=daoimpl.generateContent(diagram, table, compositeIdClassName);
		daoimpl.writeOut(contents.toString());
	}

}
