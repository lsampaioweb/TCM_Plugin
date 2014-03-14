package tcm_plugin.constants;

import tcm_plugin.Activator;

/**
 * @author Luciano Sampaio
 */
public abstract class Constants {

  public abstract class SecurityVulnerabilities {
    public static final String FIELD_SQL_INJECTION        = Activator.PLUGIN_ID + ".SQLInjection";
    public static final String FIELD_COOKIE_POISONING     = Activator.PLUGIN_ID + ".CookiePoisoning";
    public static final String FIELD_CROSS_SITE_SCRIPTING = Activator.PLUGIN_ID + ".CrossSiteScripting";

    public static final String JDT_NATURE                 = "org.eclipse.jdt.core.javanature";
    public static final String SEPARATOR                  = ";";
    public static final String FIELD_MONITORED_PLUGINS    = Activator.PLUGIN_ID + ".MonitoredPlugins";
  }

  public abstract class Settings {
    public static final String PAGE_ID                    = "tcm_plugin.ui.preference.page.settings";

    public static final String FIELD_RUN_MODE             = Activator.PLUGIN_ID + ".RunMode";

    public static final String FIELD_OUTPUT_PROBLEMS_VIEW = Activator.PLUGIN_ID + ".ProblemsView";
    public static final String FIELD_OUTPUT_TEXT_FILE     = Activator.PLUGIN_ID + ".TextFile";
    public static final String FIELD_OUTPUT_XML_FILE      = Activator.PLUGIN_ID + ".XmlFile";
  }

}
