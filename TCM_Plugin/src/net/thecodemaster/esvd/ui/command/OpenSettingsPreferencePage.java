package net.thecodemaster.esvd.ui.command;

import net.thecodemaster.esvd.constant.Constant;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Creates a workbench preference dialog and selects the our settings preference page.
 * 
 * @author Luciano Sampaio
 */
public class OpenSettingsPreferencePage extends AbstractCommand {

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		// Return the active workbench window.
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Creates a workbench preference dialog and selects our settings preference page.
		final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(activeWorkbenchWindow.getShell(),
				Constant.PrefPageSettings.ID_PAGE, null, null);
		dialog.open();

		return null;
	}

}
