package net.thecodemaster.evd.ui.l10n;

import net.thecodemaster.evd.constant.Constant;

import org.eclipse.osgi.util.NLS;

/**
 * @author Luciano Sampaio
 */
public abstract class Messages extends NLS {

	public static abstract class PrefPageAcknowledgements {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".pref_page_acknowledgements"; //$NON-NLS-1$
		public static String				DESCRIPTION;

		public static String				LABEL_GROUP_AUTHORS;
		public static String				AUTHOR_1;

		public static String				LABEL_GROUP_CONTRIBUTORS;
		public static String				CONTRIBUTORS_1;
		public static String				CONTRIBUTORS_2;

		public static String				URL_THECODEMASTER;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, PrefPageAcknowledgements.class);
		}
	}

	public static abstract class PrefPageSecurityVulnerability {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".pref_page_security_vulnerability"; //$NON-NLS-1$
		public static String				LABEL_COOKIE_POISONING;
		public static String				LABEL_CROSS_SITE_SCRIPTING;
		public static String				LABEL_SECURITY_VULNERABILITIES;
		public static String				LABEL_SQL_INJECTION;
		public static String				LABEL_SECURITY_MISCONFIGURATION;
		public static String				LABEL_MONITORED_PROJECTS;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, PrefPageSecurityVulnerability.class);
		}
	}

	public static abstract class PrefPageSettings {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".pref_page_settings"; //$NON-NLS-1$
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
			NLS.initializeMessages(BUNDLE_NAME, PrefPageSettings.class);
		}
	}

	public static abstract class Plugin {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".plugin"; //$NON-NLS-1$
		public static String				JOB;
		public static String				TASK;

		public static String				VERIFIER_NAME_SECURITY_MISCONFIGURATION;
		public static String				VERIFIER_NAME_COOKIE_POISONING;
		public static String				VERIFIER_NAME_SQL_INJECTION;
		public static String				VERIFIER_NAME_CROSS_SITE_SCRIPTING;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, Plugin.class);
		}
	}

	public static abstract class VerifierSecurityVulnerability {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".verifier_security_vulnerability";	//$NON-NLS-1$

		public static String				LITERAL;
		public static String				NULL_LITERAL;
		public static String				ENTRY_POINT_METHOD;
		public static String				LABEL_RESOLUTION;
		public static String				DESCRIPTION_RESOLUTION;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, VerifierSecurityVulnerability.class);
		}
	}

	public static abstract class Error {
		private static final String	BUNDLE_NAME	= Constant.Package.L10N_MESSAGES + ".error";	//$NON-NLS-1$

		public static String				FILE_NOT_FOUND;
		public static String				FILE_XML_PARSING_FAIL;
		public static String				FILE_XML_READING_FAIL;
		public static String				CALL_GRAPH_DOES_NOT_CONTAIN_PROJECT;

		static {
			// Initialize resource bundle.
			NLS.initializeMessages(BUNDLE_NAME, Error.class);
		}
	}

}
