package net.thecodemaster.sap.constants;

import net.thecodemaster.sap.Activator;

/**
 * This class contains constants used by the application.
 * 
 * @author Luciano Sampaio
 */
public abstract class Constants {

  public static final String JDT_NATURE                         = "org.eclipse.jdt.core.javanature";
  public static final String NATURE_ID                          = Activator.PLUGIN_ID + ".Nature";
  public static final String BUILDER_ID                         = Activator.PLUGIN_ID + ".Builder";
  public static final String MARKER_ID                          = Activator.PLUGIN_ID + ".TCMSAMarker";

  public static final String SEPARATOR                          = ";";
  public static final String RESOURCE_TYPE_TO_PERFORM_DETECTION = "java" + SEPARATOR + "jsp";
  public static final String PACKAGE_UI                         = "net.thecodemaster.sap.ui";
  public static final String PACKAGE_L10N_MESSAGES              = PACKAGE_UI + ".l10n.messages";
  public static final int    MAXIMUM_DEPTH                      = 4;

  public abstract class SecurityVulnerabilities {
    public static final String FIELD_SQL_INJECTION             = Activator.PLUGIN_ID + ".SQLInjection";
    public static final String FIELD_COOKIE_POISONING          = Activator.PLUGIN_ID + ".CookiePoisoning";
    public static final String FIELD_CROSS_SITE_SCRIPTING      = Activator.PLUGIN_ID + ".CrossSiteScripting";
    public static final String FIELD_SECURITY_MISCONFIGURATION = Activator.PLUGIN_ID + ".SecurityMisconfiguration";

    public static final String FIELD_MONITORED_PROJECTS        = Activator.PLUGIN_ID + ".MonitoredProjects";
  }

  public abstract class Settings {
    public static final String PAGE_ID                    = PACKAGE_UI + ".preference.page.settings";

    public static final String FIELD_RUN_MODE             = Activator.PLUGIN_ID + ".RunMode";

    public static final String FIELD_OUTPUT_PROBLEMS_VIEW = Activator.PLUGIN_ID + ".ProblemsView";
    public static final String FIELD_OUTPUT_TEXT_FILE     = Activator.PLUGIN_ID + ".TextFile";
    public static final String FIELD_OUTPUT_XML_FILE      = Activator.PLUGIN_ID + ".XmlFile";
  }

  public abstract class Marker {
    public static final String TYPE_SECURITY_VULNERABILITY = "TCMSA_TYPE_SECURITY_VULNERABILITY";
  }

  public abstract class Plugin {
    public static final int COOKIE_POISONING_VERIFIER_ID          = 1;
    public static final int SECURITY_MISCONFIGURATION_VERIFIER_ID = 2;
    public static final int SQL_INJECTION_VERIFIER_ID             = 3;
    public static final int XSS_VERIFIER_ID                       = 4;
  }

  public abstract class Icons {
    public static final String ICONS_FOLDER           = "icons/";
    public static final String SECURITY_VULNERABILITY = ICONS_FOLDER + "SecurityVulnerability.png";
  }

}
