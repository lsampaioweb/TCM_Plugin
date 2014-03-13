package tcm_plugin.ui.preference.page;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.ui.IWorkbenchPreferencePage;

import tcm_plugin.Activator;
import tcm_plugin.constants.Constants;
import tcm_plugin.l10n.Messages;
import tcm_plugin.utils.UtilsPreferencePage;

public class SecurityVulnerabilities extends PreferencePage implements IWorkbenchPreferencePage {

  private Table              projectsList;
  private BooleanFieldEditor ckbtnSQLInjection;

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
    setDescription(Messages.SecurityVulnerabilities.DESCRIPTION);
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
    groupOutput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    groupOutput.setText(Messages.Settings.OUTPUT_LABEL);

    // The output options where the warnings of security vulnerabilities will be displayed.
    ckbtnSQLInjection =
      UtilsPreferencePage.createBooleanField(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION, Messages.SecurityVulnerabilities.SQL_INJECTION_LABEL,
        groupOutput, getPreferenceStore());
  }

  private void createMonitoredProjectsSelection(Composite composite) {
    Label label = new Label(composite, SWT.NONE);
    label.setText("Projects that are being monitored:");
    label.setFont(composite.getFont());
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    projectsList = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);
    projectsList.setFont(composite.getFont());
    projectsList.setLayoutData(new GridData(GridData.FILL_BOTH));

    populateProjectsList();
  }

  private void populateProjectsList() {
    IProject[] projects = getListOfProjectsInWorkspace();
    String JDT_NATURE = "org.eclipse.jdt.core.javanature";

    for (IProject project : projects) {
      // check if we have a Java project
      try {
        if (project.isNatureEnabled(JDT_NATURE)) {

          TableItem item = new TableItem(projectsList, SWT.NONE);

          item.setText(project.getName());
          item.setData(project.getName());
          item.setChecked(true);
        }
      }
      catch (CoreException e) {
        // TODO
        e.printStackTrace();
      }
    }
  }

  private IProject[] getListOfProjectsInWorkspace() {
    // Get the root of the workspace.
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();

    // Return the list of all projects in the current workspace.
    return root.getProjects();
  }

  @Override
  public IPreferenceStore getPreferenceStore() {
    IPreferenceStore store = super.getPreferenceStore();

    if (null == store) {
      setPreferenceStore(Activator.getDefault().getPreferenceStore());
      store = super.getPreferenceStore();
    }

    return store;
  }

  @Override
  public void performDefaults() {
    IPreferenceStore store = getPreferenceStore();

    store.setDefault(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING, true);

    UtilsPreferencePage.loadDefaultValue(ckbtnSQLInjection);

    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    // Save (store) the content chosen by the developer back to the eclipse's preferences. 
    UtilsPreferencePage.storeValue(ckbtnSQLInjection);

    return super.performOk();
  }
}
