package tcm_plugin.utils;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Luciano Sampaio
 */
public class UtilsPreferencePage {

  /**
   * Create a HTML link to be displayed in the preference page.
   * 
   * @param link the link of the url.
   * @return a string containing the link with the HTML tags.
   */
  public static String getLinkHTML(String link) {
    return getLinkHTML(link, link);
  }

  /**
   * Create a HTML link to be displayed in the preference page.
   * 
   * @param link the link of the url.
   * @param text the text that will be display in the link.
   * @return a string containing the link with the HTML tags.
   */
  public static String getLinkHTML(String link, String text) {
    return String.format("<a href=\"http://%s\">%s</a>", link, text);
  }

  /**
   * Store the content of the field in the preference store.
   * 
   * @param fieldEditor The field editor that will have its value stored into the preferences store.
   * @return True if the operation was successful, otherwise false.
   */
  public static boolean storeValue(FieldEditor fieldEditor) {
    if (null != fieldEditor) {
      fieldEditor.store();
      return true;
    }
    return false;
  }

  /**
   * Load the default value of the field from the preference store.
   * 
   * @param fieldEditor The field editor that will have its value loaded from the preferences store.
   * @return True if the operation was successful, otherwise false.
   */
  public static boolean loadDefaultValue(FieldEditor fieldEditor) {
    if (null != fieldEditor) {
      fieldEditor.loadDefault();
      return true;
    }
    return false;
  }

  /**
   * Create a boolean field based on the current parameters
   * 
   * @param name The name (unique) of the field.
   * @param label The label of the field that will be displayed to the user.
   * @param parent The composite where the field will be inserted into.
   * @param store The preference store that contains the values of the field.
   * @return The instance of the field which was just created.
   */
  public static BooleanFieldEditor createBooleanField(String name, String label, Composite parent, IPreferenceStore store) {
    BooleanFieldEditor booleanField = new BooleanFieldEditor(name, label, parent);
    booleanField.setPreferenceStore(store);
    booleanField.load();

    return booleanField;
  }
}
