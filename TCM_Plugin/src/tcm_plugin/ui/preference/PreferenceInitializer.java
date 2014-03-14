package tcm_plugin.ui.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import tcm_plugin.ui.preference.page.SecurityVulnerabilities;
import tcm_plugin.ui.preference.page.Settings;

/**
 * It initialize the fields of our preferences pages with its default values.
 * 
 * @author Luciano Sampaio
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    SecurityVulnerabilities securityVulnerabilitiesPage = new SecurityVulnerabilities();
    securityVulnerabilitiesPage.performDefaults();

    Settings settingsPage = new Settings();
    settingsPage.performDefaults();
  }
}
