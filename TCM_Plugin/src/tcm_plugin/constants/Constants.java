package tcm_plugin.constants;

/**
 * @author Luciano Sampaio
 */
public abstract class Constants {

  public abstract class SecurityVulnerabilities {
    public static final String FIELD_SQL_INJECTION        = "SQLInjection";
    public static final String FIELD_COOKIE_POISONING     = "CookiePoisoning";
    public static final String FIELD_CROSS_SITE_SCRIPTING = "CrossSiteScripting";
  }

  public abstract class Settings {
    public static final String FIELD_RUN_MODE             = "RunMode";
    public static final String FIELD_OUTPUT_PROBLEMS_VIEW = "ProblemsView";
    public static final String FIELD_OUTPUT_TEXT_FILE     = "TextFile";
    public static final String FIELD_OUTPUT_XML_FILE      = "XmlFile";

  }

}
