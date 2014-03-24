package net.thecodemaster.sap;

import net.thecodemaster.sap.analyzers.CookiePoisoningAnalyzer;
import net.thecodemaster.sap.analyzers.ManagerAnalyzer;
import net.thecodemaster.sap.analyzers.SQLInjectionAnalyzer;
import net.thecodemaster.sap.analyzers.XSSAnalyzer;
import net.thecodemaster.sap.constants.Constants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String     PLUGIN_ID = "TheCodeMasterSecurityAnalyzerPlugin"; //$NON-NLS-1$

  // The shared instance
  private static Activator       plugin;

  // This object controls which analyzers are going to be executed to perform the security vulnerability
  // detection.
  private static ManagerAnalyzer managerAnalyzer;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  public static void resetManagerAnalyzer() {
    managerAnalyzer = null;
  }

  public static ManagerAnalyzer getManagerAnalyzer() {
    if (null == managerAnalyzer) {
      managerAnalyzer = new ManagerAnalyzer();

      IPreferenceStore store = getDefault().getPreferenceStore();

      // Get the options checked by the developer.
      boolean cookiePoisoning = store.getBoolean(Constants.SecurityVulnerabilities.FIELD_COOKIE_POISONING);
      boolean crossSiteScripting =
        store.getBoolean(Constants.SecurityVulnerabilities.FIELD_CROSS_SITE_SCRIPTING);
      boolean sqlInjection = store.getBoolean(Constants.SecurityVulnerabilities.FIELD_SQL_INJECTION);

      if (cookiePoisoning) {
        managerAnalyzer.addAnalyzer(new CookiePoisoningAnalyzer());
      }
      if (crossSiteScripting) {
        managerAnalyzer.addAnalyzer(new XSSAnalyzer());
      }
      if (sqlInjection) {
        managerAnalyzer.addAnalyzer(new SQLInjectionAnalyzer());
      }
    }

    return managerAnalyzer;
  }

}
