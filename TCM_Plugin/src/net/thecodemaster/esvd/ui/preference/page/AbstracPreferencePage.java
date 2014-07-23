package net.thecodemaster.esvd.ui.preference.page;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.helper.HelperProjects;
import net.thecodemaster.esvd.logger.PluginLogger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Luciano Sampaio
 */
public abstract class AbstracPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Creates a new preference page with an empty title and no image.
	 */
	public AbstracPreferencePage() {
		super();
	}

	/**
	 * Creates a new preference page with the given title and no image.
	 * 
	 * @param title
	 *          the title of this preference page
	 */
	public AbstracPreferencePage(String title) {
		super(title);
	}

	/**
	 * Creates a new abstract preference page with the given title and image.
	 * 
	 * @param title
	 *          the title of this preference page
	 * @param image
	 *          the image for this preference page, or null if none
	 */
	public AbstracPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		IPreferenceStore store = super.getPreferenceStore();

		if (null == store) {
			setPreferenceStore(Activator.getDefault().getPreferenceStore());
			store = super.getPreferenceStore();
		}

		return store;
	}

	/**
	 * Create a HTML link to be displayed in the preference page.
	 * 
	 * @param link
	 *          the link of the url.
	 * @return a string containing the link with the HTML tags.
	 */
	protected String getLinkHTML(String link) {
		return getLinkHTML(link, link);
	}

	/**
	 * Create a HTML link to be displayed in the preference page.
	 * 
	 * @param link
	 *          the link of the url.
	 * @param text
	 *          the text that will be display in the link.
	 * @return a string containing the link with the HTML tags.
	 */
	protected String getLinkHTML(String link, String text) {
		return String.format("<a href=\"%s\">%s</a>", link, text);
	}

	/**
	 * Store the content of the field in the preference store.
	 * 
	 * @param fieldEditor
	 *          The field editor that will have its value stored into the preferences store.
	 * @return True if the operation was successful, otherwise false.
	 */
	protected boolean storeValue(FieldEditor fieldEditor) {
		if (null != fieldEditor) {
			fieldEditor.store();
			return true;
		}
		return false;
	}

	/**
	 * Load the default value of the field from the preference store.
	 * 
	 * @param fieldEditor
	 *          The field editor that will have its value loaded from the preferences store.
	 * @return True if the operation was successful, otherwise false.
	 */
	protected boolean loadDefaultValue(FieldEditor fieldEditor) {
		if (null != fieldEditor) {
			fieldEditor.loadDefault();
			return true;
		}
		return false;
	}

	/**
	 * Create a boolean field based on the current parameters
	 * 
	 * @param name
	 *          The name (unique) of the field.
	 * @param label
	 *          The label of the field that will be displayed to the user.
	 * @param parent
	 *          The composite where the field will be inserted into.
	 * @return The instance of the field which was just created.
	 */
	protected BooleanFieldEditor createBooleanField(String name, String label, Composite parent) {
		BooleanFieldEditor booleanField = new BooleanFieldEditor(name, label, parent);
		booleanField.setPreferenceStore(getPreferenceStore());
		booleanField.load();

		return booleanField;
	}

	/**
	 * Opens a URL on this Web browser instance.
	 * 
	 * @param URL
	 *          the URL to open.
	 * @return This adapter class provides default implementations for the methods described by the SelectionListener
	 *         interface.
	 */
	protected SelectionAdapter onClickOpenURL(final String URL) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(URL));
				} catch (PartInitException | MalformedURLException e1) {
					PluginLogger.logError(e1);
				}
			}
		};
	}

	/**
	 * Returns the collection of projects which exist under this root. <br/>
	 * This collection has only Java projects and which are accessible and opened.
	 * 
	 * @return An list of projects.
	 */
	protected List<IProject> getListOfProjectsInWorkspace() {
		return HelperProjects.getProjectsInWorkspace();
	}

	/**
	 * Returns a collection containing the projects that are being monitored by our plug-in.
	 * 
	 * @return A collection of projects' names.
	 */
	protected List<IProject> getListOfMonitoredProjects() {
		return HelperProjects.getMonitoredProjects();
	}

	/**
	 * @param projects
	 */
	protected void setProjectsToListOfMonitoredProjects(List<IProject> projects) {
		HelperProjects.setProjectsToListOfMonitoredProjects(projects);
	}

	protected void resetPluginState() {
		HelperProjects.resetPluginState();
	}

}
