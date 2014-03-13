package tcm_plugin.ui.preference.page;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;

import tcm_plugin.Activator;
import tcm_plugin.constants.Constants;
import tcm_plugin.l10n.Messages;
import tcm_plugin.utils.Utils;

public class SecurityVulnerabilities extends TCMPreferencePage {

  private Table              projectsList;
  private BooleanFieldEditor ckbtnSQLInjection;
  private BooleanFieldEditor ckbtnCookiePoisoning;
  private BooleanFieldEditor ckbtnCrossSiteScripting;

  public SecurityVulnerabilities() {
  }

  public SecurityVulnerabilities(String title) {
    super(title);
  }

  public SecurityVulnerabilities(String title, ImageDescriptor image) {
    super(title, image);
  }

  @Override
  public void init(IWorkbench workbench) {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite composite = createComposite(parent);

    createSecurityVulnerabilityOptions(composite);
    createMonitoredProjectsSelection(composite);

    return composite;
  }

  protected Composite createComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);

    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);

    GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
    composite.setLayoutData(data);
    composite.setFont(parent.getFont());

    return composite;
  }

  private void createSecurityVulnerabilityOptions(Composite composite) {
    // Group output option and its children.
    Group groupOutput = new Group(composite, SWT.NONE);
    groupOutput.setLayout(new GridLayout());
    groupOutput.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
    groupOutput.setText(Messages.SecurityVulnerabilities.SECURITY_VULNERABILITIES_LABEL);

    // The security vulnerabilities that will be detected in the source code.
    ckbtnSQLInjection =
      createBooleanField(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION, Messages.SecurityVulnerabilities.SQL_INJECTION_LABEL, groupOutput);
    ckbtnCookiePoisoning =
      createBooleanField(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING, Messages.SecurityVulnerabilities.COOKIE_POISONING_LABEL, groupOutput);
    ckbtnCrossSiteScripting =
      createBooleanField(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING, Messages.SecurityVulnerabilities.CROSS_SITE_SCRIPTING_LABEL, groupOutput);
  }

  private void createMonitoredProjectsSelection(Composite composite) {
    Label label = new Label(composite, SWT.NONE);
    label.setText(Messages.SecurityVulnerabilities.MONITORED_PROJECTS_LABEL);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    projectsList = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);
    projectsList.setFont(composite.getFont());
    projectsList.setLayoutData(new GridData(GridData.FILL_BOTH));

    populateProjectsList();
  }

  private void populateProjectsList() {
    List<IProject> projects = getListOfJavaProjectsInWorkspace();

    IPreferenceStore store = getPreferenceStore();
    List<String> monitoredPlugins =
      Utils.getListFromString(store.getString(Constants.SecurityVulnerabilities.FIELD_MONITORED_PLUGINS), Constants.SecurityVulnerabilities.SEPARATOR);

    for (IProject project : projects) {
      TableItem item = new TableItem(projectsList, SWT.NONE);

      item.setText(project.getName());
      item.setData(project.getName());
      // If the current project is inside the list of monitored projects, then this project should be checked.
      item.setChecked(monitoredPlugins.contains(project.getName()));
    }
  }

  @Override
  public void performDefaults() {
    IPreferenceStore store = getPreferenceStore();

    store.setDefault(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING, true);

    loadDefaultValue(ckbtnSQLInjection);
    loadDefaultValue(ckbtnCookiePoisoning);
    loadDefaultValue(ckbtnCrossSiteScripting);

    if (null != projectsList) {
      // It will iterate over all the projects in the workspace and check it, so the plug-in will scan the project.
      TableItem items[] = projectsList.getItems();
      for (TableItem item : items) {
        item.setChecked(true);
      }
    }

    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    // Save (store) the content chosen by the developer back to the eclipse's preferences. 
    storeValue(ckbtnSQLInjection);
    storeValue(ckbtnCookiePoisoning);
    storeValue(ckbtnCrossSiteScripting);

    StringBuffer monitoredPlugins = new StringBuffer();
    TableItem items[] = projectsList.getItems();
    for (TableItem item : items) {
      if (item.getChecked()) {
        monitoredPlugins.append((String) item.getData());
        monitoredPlugins.append(Constants.SecurityVulnerabilities.SEPARATOR);
      }
    }
    IPreferenceStore store = getPreferenceStore();
    store.putValue(Constants.SecurityVulnerabilities.FIELD_MONITORED_PLUGINS, monitoredPlugins.toString());

    return super.performOk();
  }
}
