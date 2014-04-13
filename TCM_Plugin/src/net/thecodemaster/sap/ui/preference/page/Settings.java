package net.thecodemaster.sap.ui.preference.page;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;

public class Settings extends AbstracPreferencePage {

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
      new String[][] { { Messages.Settings.LABEL_RUN_AUTOMATICALLY, Messages.Settings.VALUE_RUN_AUTOMATICALLY },
          { Messages.Settings.LABEL_RUN_ON_SAVE, Messages.Settings.VALUE_RUN_ON_SAVE },
          { Messages.Settings.LABEL_RUN_MANUALLY, Messages.Settings.VALUE_RUN_MANUALLY } };

    // Group run mode and its children.
    rbtnRunMode = new RadioGroupFieldEditor(Constants.Settings.FIELD_RUN_MODE, Messages.Settings.LABEL_RUN_MODE, 3, data, top, true);
    rbtnRunMode.setPreferenceStore(getPreferenceStore());
    rbtnRunMode.load();

    // Group output option and its children.
    Group groupOutput = new Group(top, SWT.NONE);
    groupOutput.setLayout(new GridLayout());
    groupOutput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    groupOutput.setText(Messages.Settings.LABEL_OUTPUT);

    // The output options where the warnings of security vulnerabilities will be displayed.
    ckbtnProblemView = createBooleanField(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW, Messages.Settings.LABEL_OUTPUT_PROBLEMS_VIEW, groupOutput);
    ckbtnTextFile = createBooleanField(Constants.Settings.FIELD_OUTPUT_TEXT_FILE, Messages.Settings.LABEL_OUTPUT_TEXT_FILE, groupOutput);
    ckbtnXmlFile = createBooleanField(Constants.Settings.FIELD_OUTPUT_XML_FILE, Messages.Settings.LABEL_OUTPUT_XML_FILE, groupOutput);

    return top;
  }

  @Override
  public void performDefaults() {
    IPreferenceStore store = getPreferenceStore();
    // Save the default values into the preference file.
    store.setDefault(Constants.Settings.FIELD_RUN_MODE, Messages.Settings.VALUE_RUN_AUTOMATICALLY);

    store.setDefault(Constants.Settings.FIELD_OUTPUT_PROBLEMS_VIEW, true);
    store.setDefault(Constants.Settings.FIELD_OUTPUT_TEXT_FILE, false);
    store.setDefault(Constants.Settings.FIELD_OUTPUT_XML_FILE, false);

    // Set the default values into the fields.
    loadDefaultValue(rbtnRunMode);

    loadDefaultValue(ckbtnProblemView);
    loadDefaultValue(ckbtnTextFile);
    loadDefaultValue(ckbtnXmlFile);

    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    // Save (store) the content chosen by the developer back to the eclipse's preferences. 
    storeValue(rbtnRunMode);

    storeValue(ckbtnProblemView);
    storeValue(ckbtnTextFile);
    storeValue(ckbtnXmlFile);

    return super.performOk();
  }
}
