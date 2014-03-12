package tcm_plugin.ui.preference.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tcm_plugin.Activator;
import tcm_plugin.constants.Constants;
import tcm_plugin.l10n.Messages;
import tcm_plugin.utils.UtilsPreferencePage;

public class Settings extends PreferencePage implements IWorkbenchPreferencePage {

  private RadioGroupFieldEditor rbtnRunMode;
  private BooleanFieldEditor    ckbtnProblemView;
  private BooleanFieldEditor    ckbtnTextFile;
  private BooleanFieldEditor    ckbtnXmlFile;

  public Settings() {
  }

  public Settings(String title) {
    super(title);
  }

  public Settings(String title, ImageDescriptor image) {
    super(title, image);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(IWorkbench workbench) {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription(Messages.Settings.DESCRIPTION);
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite top = new Composite(parent, SWT.LEFT);

    // Sets the layout data for the top composite's place in its parent's layout.
    top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Sets the layout for the top composite's children to populate.
    top.setLayout(new GridLayout());

    String[][] data =
      new String[][] { { Messages.Settings.RUN_AUTOMATICALLY_LABEL, Messages.Settings.RUN_AUTOMATICALLY_VALUE },
          { Messages.Settings.RUN_ON_SAVE_LABEL, Messages.Settings.RUN_ON_SAVE_VALUE },
          { Messages.Settings.RUN_MANUALLY_LABEL, Messages.Settings.RUN_MANUALLY_VALUE } };

    // Group run mode and its children.
    rbtnRunMode = new RadioGroupFieldEditor(Constants.Settings.FIELD_RUN_MODE, Messages.Settings.RUN_MODE_LABEL, 3, data, top, true);
    rbtnRunMode.setPreferenceStore(getPreferenceStore());
    rbtnRunMode.load();

    // Group output option and its children.
    Group groupOutput = new Group(top, SWT.NONE);
    groupOutput.setLayout(new GridLayout());
    groupOutput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    groupOutput.setText(Messages.Settings.OUTPUT_LABEL);

    // The output options where the warnings of security vulnerabilities will be displayed.
    ckbtnProblemView =
      UtilsPreferencePage.createBooleanField(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW, Messages.Settings.OUTPUT_PROBLEMS_VIEW_LABEL, groupOutput,
        getPreferenceStore());
    ckbtnTextFile =
      UtilsPreferencePage.createBooleanField(Constants.Settings.FIELD_OUTPUT_TEXT_FILE, Messages.Settings.OUTPUT_TEXT_FILE_LABEL, groupOutput,
        getPreferenceStore());
    ckbtnXmlFile =
      UtilsPreferencePage.createBooleanField(Constants.Settings.FIELD_OUTPUT_XML_FILE, Messages.Settings.OUTPUT_XML_FILE_LABEL, groupOutput,
        getPreferenceStore());

    return top;
  }

  @Override
  public boolean performOk() {
    // Save (store) the content chosen by the developer back to the eclipse's preferences. 
    UtilsPreferencePage.storeValue(rbtnRunMode);
    UtilsPreferencePage.storeValue(ckbtnProblemView);
    UtilsPreferencePage.storeValue(ckbtnTextFile);
    UtilsPreferencePage.storeValue(ckbtnXmlFile);

    return super.performOk();
  }

  @Override
  public void performDefaults() {
    IPreferenceStore store = getPreferenceStore();
    // Save the default values into the preference file.
    store.setDefault(Constants.Settings.FIELD_RUN_MODE, Messages.Settings.RUN_AUTOMATICALLY_VALUE);

    store.setDefault(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW, true);
    store.setDefault(Constants.Settings.FIELD_OUTPUT_TEXT_FILE, false);
    store.setDefault(Constants.Settings.FIELD_OUTPUT_XML_FILE, false);

    // Set the default values into the fields.
    UtilsPreferencePage.loadDefaultValue(rbtnRunMode);
    UtilsPreferencePage.loadDefaultValue(ckbtnProblemView);
    UtilsPreferencePage.loadDefaultValue(ckbtnTextFile);
    UtilsPreferencePage.loadDefaultValue(ckbtnXmlFile);

    super.performDefaults();
  }

}
