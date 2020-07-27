package org.insightech.er.editor.model.settings;

import org.eclipse.swt.widgets.Event;
import org.insightech.er.ResourceString;
import org.insightech.er.editor.ERDiagramEditor;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.view.action.AbstractBaseAction;

public class LogicalName2DescribeAction extends AbstractBaseAction{

	public static final String ID = LogicalName2DescribeAction.class.getName();

	public LogicalName2DescribeAction(ERDiagramEditor editor) {
		super(ID, ResourceString.getResourceString("action.title.settings.physical2desc"),
				editor);
		 
	}

	@Override
	public void execute(Event event) {
		ERDiagram diagram = this.getDiagram();
		LogicalName2Describe phyname2Desc=new LogicalName2Describe();
		phyname2Desc.replaceLogicalName(diagram);
		this.refreshProject();
		diagram.refreshVisuals();
		//JavaFileEditorInput
	}
}
