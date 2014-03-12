package tcm_plugin.ui.preference.page;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tcm_plugin.Activator;
import tcm_plugin.constants.Constants;
import tcm_plugin.l10n.Messages;
import tcm_plugin.utils.UtilsPreferencePage;

public class SecurityVulnerabilities extends PreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor ckbtnProblemView;

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
    Composite top = new Composite(parent, SWT.LEFT);

    // Sets the layout data for the top composite's place in its parent's layout.
    top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Sets the layout for the top composite's children to populate.
    top.setLayout(new GridLayout());

    // Group output option and its children.
    Group groupOutput = new Group(top, SWT.NONE);
    groupOutput.setLayout(new GridLayout());
    groupOutput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    groupOutput.setText(Messages.Settings.OUTPUT_LABEL);

    Text text = new Text(top, NONE);

    // The output options where the warnings of security vulnerabilities will be displayed.
    ckbtnProblemView =
      UtilsPreferencePage.createBooleanField(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW, Messages.Settings.OUTPUT_PROBLEMS_VIEW_LABEL, groupOutput,
        getPreferenceStore());

    return top;
  }

  @Override
  public boolean performOk() {
    // Save (store) the content chosen by the developer back to the eclipse's preferences. 
    UtilsPreferencePage.storeValue(ckbtnProblemView);

    return super.performOk();
  }

  @Override
  public void performDefaults() {
    IPreferenceStore store = getPreferenceStore();

    store.setDefault(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING, true);
    store.setDefault(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING, true);

    UtilsPreferencePage.loadDefaultValue(ckbtnProblemView);

    super.performDefaults();
  }

}
