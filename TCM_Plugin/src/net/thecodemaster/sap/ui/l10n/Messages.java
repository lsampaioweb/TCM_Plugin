package net.thecodemaster.sap.ui.l10n;

import net.thecodemaster.sap.constants.Constants;

import org.eclipse.osgi.util.NLS;

/**
 * @author Luciano Sampaio
 */
public abstract class Messages extends NLS {

	public static abstract class AcknowledgementsPrefPage {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".acknowledgements_pref_page";	//$NON-NLS-1$
		public static String				DESCRIPTION;

		public static String				LABEL_GROUP_AUTHORS;
		public static String				AUTHOR_1;

		public static String				LABEL_GROUP_CONTRIBUTORS;
		public static String				CONTRIBUTORS_1;
		public static String				CONTRIBUTORS_2;

		public static String				URL_THECODEMASTER;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, AcknowledgementsPrefPage.class);
		}
	}

	public static abstract class SecurityVulnerabilitiesPrefPage {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".security_vulnerabilities_pref_page";	//$NON-NLS-1$
		public static String				LABEL_SECURITY_VULNERABILITIES;
		public static String				LABEL_SQL_INJECTION;
		public static String				LABEL_COOKIE_POISONING;
		public static String				LABEL_CROSS_SITE_SCRIPTING;
		public static String				LABEL_SECURITY_MISCONFIGURATION;
		public static String				LABEL_MONITORED_PROJECTS;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, SecurityVulnerabilitiesPrefPage.class);
		}
	}

	public static abstract class SettingsPrefPage {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".settings_pref_page";	//$NON-NLS-1$
		public static String				DESCRIPTION;
		public static String				LABEL_RUN_MODE;

		// Run mode text
		public static String				LABEL_RUN_AUTOMATICALLY;
		public static String				VALUE_RUN_AUTOMATICALLY;
		public static String				LABEL_RUN_ON_SAVE;
		public static String				VALUE_RUN_ON_SAVE;
		public static String				LABEL_RUN_MANUALLY;
		public static String				VALUE_RUN_MANUALLY;

		// Output text
		public static String				LABEL_OUTPUT;
		public static String				LABEL_OUTPUT_PROBLEMS_VIEW;
		public static String				LABEL_OUTPUT_TEXT_FILE;
		public static String				LABEL_OUTPUT_XML_FILE;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, SettingsPrefPage.class);
		}
	}

	public static abstract class Plugin {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".plugin";	//$NON-NLS-1$
		public static String				JOB;
		public static String				TASK;

		public static String				SECURITY_MISCONFIGURATION_VERIFIER_NAME;
		public static String				COOKIE_POISONING_VERIFIER_NAME;
		public static String				SQL_INJECTION_VERIFIER_NAME;
		public static String				XSS_VERIFIER_NAME;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, Plugin.class);
		}
	}

	public static abstract class SecurityVulnerabilitiesVerifier {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".security_vulnerabilities_verifier"; //$NON-NLS-1$

		public static String				LITERAL;
		public static String				NULL_LITERAL;
		public static String				ENTRY_POINT_METHOD;
		public static String				LABEL_RESOLUTION;
		public static String				DESCRIPTION_RESOLUTION;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, SecurityVulnerabilitiesVerifier.class);
		}
	}

	public static abstract class Error {
		private static final String	BUNDLE_NAME	= Constants.PACKAGE_L10N_MESSAGES + ".error"; //$NON-NLS-1$

		public static String				FILE_NOT_FOUND;
		public static String				PARSING_XML_FILE;
		public static String				READING_XML_FILE;
		public static String				CALL_GRAPH_DOES_NOT_CONTAIN_PROJECT;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, Error.class);
		}
	}

}
