package org.insightech.er.editor.model.dbexport.projectcode;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.insightech.er.ResourceString;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;

/**
 * 
 * @author ljc
 *
 */
public class ExportToProjectCodeWithProgressManager extends ExportToProjectCodeManager implements IRunnableWithProgress  {
	
	private Exception exception;


	public ExportToProjectCodeWithProgressManager(ExportJavaSetting exportJavaSetting, ERDiagram diagram) {
		super(exportJavaSetting, diagram);
		// TODO Auto-generated constructor stub
	}

	private IProgressMonitor monitor;
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
		int count = this.diagram.getDiagramContents().getContents()
				.getTableSet().getList().size();

		monitor.beginTask(ResourceString.getResourceString("dialog.message.export.projectcode"), count);

		try {
			this.monitor = monitor;
			doProcess();

		} catch (InterruptedException e) {
			throw e;

		} catch (Exception e) {
			this.exception = e;
		}

		monitor.done();
	}
	
	@Override
	protected void doPreTask(ERTable table) {
		this.monitor.subTask("writing : " + ProjectCodeCommon.getClassName(table));
	}

	@Override
	protected void doPostTask() throws InterruptedException {
		this.monitor.worked(1);

		if (this.monitor.isCanceled()) {
			throw new InterruptedException("Cancel has been requested.");
		}
	}

	
	public Exception getException() {
		return exception;
	}
}
