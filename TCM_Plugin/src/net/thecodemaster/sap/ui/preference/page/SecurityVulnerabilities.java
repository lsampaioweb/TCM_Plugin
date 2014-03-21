package net.thecodemaster.sap.ui.preference.page;

import java.util.Collection;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.l10n.Messages;
import net.thecodemaster.sap.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
    // The collection of projects which exist under this workspace.
    Collection<IProject> projects = getListOfJavaProjectsInWorkspace();

    // The collection of projects that are being monitored by our plug-in.
    Collection<IProject> monitoredProjects = getListOfMonitoredProjects();

    for (IProject project : projects) {
      TableItem item = new TableItem(projectsList, SWT.NONE);

      item.setText(project.getName());
      item.setData(project.getName());
      // If the current project is inside the list of monitored projects, then this project should be checked.
      item.setChecked(monitoredProjects.contains(project));
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

    // The list with the projects to be monitored.
    Collection<IProject> listMonitoredProjects = Utils.newCollection();

    // Iterate over the list of selected projects and if they are checked, add them to the list.
    TableItem items[] = projectsList.getItems();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (TableItem item : items) {
      if (item.getChecked()) {
        listMonitoredProjects.add(root.getProject((String) item.getData()));
      }
    }

    // Save the list back to the preference store.
    saveListOfMonitoredProjects(listMonitoredProjects);

    return super.performOk();
  }
}
