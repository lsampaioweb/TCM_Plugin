package net.thecodemaster.esvd.ui.preference.page;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * It initialize the fields of our preferences pages with its default values.
 * 
 * @author Luciano Sampaio
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    SecurityVulnerability securityVulnerabilitiesPage = new SecurityVulnerability();
    securityVulnerabilitiesPage.performDefaults();

    Settings settingsPage = new Settings();
    settingsPage.performDefaults();
  }
}
