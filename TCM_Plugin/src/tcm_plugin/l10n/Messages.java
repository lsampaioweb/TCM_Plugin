package tcm_plugin.l10n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Luciano Sampaio
 */
public abstract class Messages extends NLS {

  public static abstract class Acknowledgements {
    private static final String BUNDLE_NAME = "tcm_plugin.l10n.messages.acknowledgements"; //$NON-NLS-1$
    public static String        DESCRIPTION;

    public static String        GROUP_AUTHORS_LABEL;
    public static String        AUTHOR_1;

    public static String        GROUP_CONTRIBUTORS_LABEL;
    public static String        CONTRIBUTORS_1;
    public static String        CONTRIBUTORS_2;

    public static String        THECODEMASTER_URL;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Acknowledgements.class);
    }
  }

  public static abstract class SecurityVulnerabilities {
    private static final String BUNDLE_NAME = "tcm_plugin.l10n.messages.securityvulnerabilities"; //$NON-NLS-1$
    public static String        DESCRIPTION;
    public static String        SQL_INJECTION_LABEL;
    public static String        COOKIE_POISONING_LABEL;
    public static String        CROSS_SITE_SCRIPTING_LABEL;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, SecurityVulnerabilities.class);
    }
  }

  public static abstract class Settings {
    private static final String BUNDLE_NAME = "tcm_plugin.l10n.messages.settings"; //$NON-NLS-1$
    public static String        DESCRIPTION;
    public static String        RUN_MODE_LABEL;

    // Run mode text
    public static String        RUN_AUTOMATICALLY_LABEL;
    public static String        RUN_AUTOMATICALLY_VALUE;
    public static String        RUN_ON_SAVE_LABEL;
    public static String        RUN_ON_SAVE_VALUE;
    public static String        RUN_MANUALLY_LABEL;
    public static String        RUN_MANUALLY_VALUE;

    // Output text
    public static String        OUTPUT_LABEL;
    public static String        OUTPUT_PROBLEMS_VIEW_LABEL;
    public static String        OUTPUT_TEXT_FILE_LABEL;
    public static String        OUTPUT_XML_FILE_LABEL;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Settings.class);
    }

  }
}
