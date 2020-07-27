package org.insightech.er.editor.view.action.dbexport;
 

import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.insightech.er.Activator;
import org.insightech.er.ImageKey;
import org.insightech.er.ResourceString;
import org.insightech.er.editor.ERDiagramEditor;
import org.insightech.er.editor.controller.command.settings.ChangeSettingsCommand;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.settings.Settings;
import org.insightech.er.editor.view.action.AbstractBaseAction;
import org.insightech.er.editor.view.dialog.dbexport.ExportToProjectCodeDialog;

/**
 * 项目导出
 * @author ljc
 *
 */
public class ExportToProjectCodeAction extends AbstractBaseAction {

	public static final String ID = ExportToProjectCodeAction.class.getName();

	public ExportToProjectCodeAction(ERDiagramEditor editor) {
		super(ID, ResourceString.getResourceString("action.title.export.projectcode"),
				editor);
		this.setImageDescriptor(Activator
				.getImageDescriptor(ImageKey.EXPORT_TO_PROJECTCODE));
	}

	@Override
	public void execute(Event event) {
		ERDiagram diagram = this.getDiagram();

		ExportToProjectCodeDialog dialog = new ExportToProjectCodeDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell(), diagram,
				this.getEditorPart());

		dialog.open();
		this.refreshProject();

		if (dialog.getExportSetting() != null
				&& !diagram.getDiagramContents().getSettings()
						.getExportSetting().equals(dialog.getExportSetting())) {
			Settings newSettings = (Settings) diagram.getDiagramContents()
					.getSettings().clone();
			newSettings.setExportSetting(dialog.getExportSetting());

			ChangeSettingsCommand command = new ChangeSettingsCommand(diagram,
					newSettings, false);
			this.execute(command);
		}
	}
}
