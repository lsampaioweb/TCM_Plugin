package net.thecodemaster.sap.ui.preference;

import net.thecodemaster.sap.ui.preference.page.SecurityVulnerabilities;
import net.thecodemaster.sap.ui.preference.page.Settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

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
