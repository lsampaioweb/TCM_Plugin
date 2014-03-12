package tcm_plugin.ui.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import tcm_plugin.ui.preference.page.SecurityVulnerabilities;
import tcm_plugin.ui.preference.page.Settings;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

  public PreferenceInitializer() {
  }

  @Override
  public void initializeDefaultPreferences() {
    SecurityVulnerabilities securityVulnerabilitiesPage = new SecurityVulnerabilities();
    securityVulnerabilitiesPage.performDefaults();

    Settings settingsPage = new Settings();
    settingsPage.performDefaults();
  }
}
