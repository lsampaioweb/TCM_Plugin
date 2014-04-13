package net.thecodemaster.sap.ui.l10n;

import net.thecodemaster.sap.constants.Constants;

import org.eclipse.osgi.util.NLS;

/**
 * @author Luciano Sampaio
 */
public abstract class Messages extends NLS {

  public static abstract class Acknowledgements {
    private static final String BUNDLE_NAME = Constants.PACKAGE_UI + ".l10n.messages.acknowledgements"; //$NON-NLS-1$
    public static String        DESCRIPTION;

    public static String        LABEL_GROUP_AUTHORS;
    public static String        AUTHOR_1;

    public static String        LABEL_GROUP_CONTRIBUTORS;
    public static String        CONTRIBUTORS_1;
    public static String        CONTRIBUTORS_2;

    public static String        URL_THECODEMASTER;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Acknowledgements.class);
    }
  }

  public static abstract class SecurityVulnerabilities {
    private static final String BUNDLE_NAME = Constants.PACKAGE_UI + ".l10n.messages.securityvulnerabilities"; //$NON-NLS-1$
    public static String        LABEL_SECURITY_VULNERABILITIES;
    public static String        LABEL_SQL_INJECTION;
    public static String        LABEL_COOKIE_POISONING;
    public static String        LABEL_CROSS_SITE_SCRIPTING;
    public static String        LABEL_SECURITY_MISCONFIGURATION;
    public static String        LABEL_MONITORED_PROJECTS;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, SecurityVulnerabilities.class);
    }
  }

  public static abstract class Settings {
    private static final String BUNDLE_NAME = Constants.PACKAGE_UI + ".l10n.messages.settings"; //$NON-NLS-1$
    public static String        DESCRIPTION;
    public static String        LABEL_RUN_MODE;

    // Run mode text
    public static String        LABEL_RUN_AUTOMATICALLY;
    public static String        VALUE_RUN_AUTOMATICALLY;
    public static String        LABEL_RUN_ON_SAVE;
    public static String        VALUE_RUN_ON_SAVE;
    public static String        LABEL_RUN_MANUALLY;
    public static String        VALUE_RUN_MANUALLY;

    // Output text
    public static String        LABEL_OUTPUT;
    public static String        LABEL_OUTPUT_PROBLEMS_VIEW;
    public static String        LABEL_OUTPUT_TEXT_FILE;
    public static String        LABEL_OUTPUT_XML_FILE;

    static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Settings.class);
    }

  }
}
