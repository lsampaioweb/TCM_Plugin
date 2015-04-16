package net.thecodemaster.esvd.constant;

import net.thecodemaster.esvd.Activator;

/**
 * This class contains constants used by the application.
 * 
 * @author Luciano Sampaio
 */
public abstract class Constant {

	public static final boolean	IS_DEBUGGING																= true;
	public static final String	JDT_NATURE																	= "org.eclipse.jdt.core.javanature";
	public static final String	NATURE_ID																		= Activator.PLUGIN_ID + ".TCM_ESVD_NATURE";
	public static final String	BUILDER_ID																	= Activator.PLUGIN_ID + ".TCM_ESVD_BUILDER";
	public static final String	VIEW_ID																			= Activator.PLUGIN_ID + ".TCM_ESVD_VIEW";
	public static final String	MARKER_ID																		= Activator.PLUGIN_ID + ".TCM_ESVD_MARKER";
	public static final String	MARKER_ID_INVISIBLE													= Activator.PLUGIN_ID
																																							+ ".TCM_ESVD_MARKER_INVISIBLE";

	public static final String	SEPARATOR_RESOURCES_TYPE										= ";";
	public static final String	SEPARATOR_FULL_PATH													= " - ";
	public static final String	RESOURCE_TYPE_TO_PERFORM_DETECTION					= "java";
	public static final String	OBJECT																			= "java.lang.Object";

	public static final int			VERIFIER_ID_COMMAND_INJECTION								= 1;
	public static final int			VERIFIER_ID_COOKIE_POISONING								= 2;
	public static final int			VERIFIER_ID_CROSS_SITE_SCRIPTING						= 3;
	public static final int			VERIFIER_ID_HTTP_RESPONSE_SPLITTING					= 4;
	public static final int			VERIFIER_ID_LDAP_INJECTION									= 5;
	public static final int			VERIFIER_ID_LOG_FORGING											= 6;
	public static final int			VERIFIER_ID_PATH_TRAVERSAL									= 7;
	public static final int			VERIFIER_ID_REFLECTION_INJECTION						= 8;
	public static final int			VERIFIER_ID_SECURITY_MISCONFIGURATION				= 9;
	public static final int			VERIFIER_ID_SQL_INJECTION										= 10;
	public static final int			VERIFIER_ID_XPATH_INJECTION									= 11;

	public static final int			RESOLUTION_ID_IGNORE_WARNING								= 20;

	// Used for ranking.
	public static final int			VERIFIER_PRIORITY_CROSS_SITE_SCRIPTING			= 1;
	public static final int			VERIFIER_PRIORITY_SQL_INJECTION							= 2;
	public static final int			VERIFIER_PRIORITY_PATH_TRAVERSAL						= 3;

	public static final int			VERIFIER_PRIORITY_COMMAND_INJECTION					= 4;
	public static final int			VERIFIER_PRIORITY_REFLECTION_INJECTION			= 5;
	public static final int			VERIFIER_PRIORITY_XPATH_INJECTION						= 6;
	public static final int			VERIFIER_PRIORITY_LDAP_INJECTION						= 7;

	public static final int			VERIFIER_PRIORITY_COOKIE_POISONING					= 8;
	public static final int			VERIFIER_PRIORITY_HTTP_RESPONSE_SPLITTING		= 9;

	public static final int			VERIFIER_PRIORITY_SECURITY_MISCONFIGURATION	= 10;
	public static final int			VERIFIER_PRIORITY_LOG_FORGING								= 11;

	public abstract class Package {
		public static final String	UI						= "net.thecodemaster.esvd.ui";
		public static final String	L10N_MESSAGES	= UI + ".l10n.message";
	}

	public abstract class Folder {
		public static final String	ICON								= "icon/";
		public static final String	KNOWLEDGE_BASE			= "knowledge_base/";
		public static final String	ENTRY_POINT					= KNOWLEDGE_BASE + "entry_point/";
		public static final String	EXIT_POINT					= KNOWLEDGE_BASE + "exit_point/";
		public static final String	RESOLUTION					= KNOWLEDGE_BASE + "resolution/";
		public static final String	SANITIZATION_POINT	= KNOWLEDGE_BASE + "sanitization_point/";
	}

	public abstract class File {
		public static final String	FILE_SANITIZATION_POINT										= Folder.SANITIZATION_POINT
																																							+ "sanitization_point.xml";
		public static final String	FILE_ENTRY_POINT													= Folder.ENTRY_POINT + "entry_point.xml";
		public static final String	FILE_RESOLUTION														= Folder.RESOLUTION + "resolution.xml";

		public static final String	FILE_EXIT_POINT_COMMAND_INJECTION					= Folder.EXIT_POINT + "command_injection.xml";
		public static final String	FILE_EXIT_POINT_COOKIE_POISONING					= Folder.EXIT_POINT + "cookie_poisoning.xml";
		public static final String	FILE_EXIT_POINT_CROSS_SITE_SCRIPTING			= Folder.EXIT_POINT
																																							+ "cross_site_scripting.xml";

		public static final String	FILE_EXIT_POINT_HTTP_RESPONSE_SPLITTING		= Folder.EXIT_POINT
																																							+ "http_response_splitting.xml";

		public static final String	FILE_EXIT_POINT_LDAP_INJECTION						= Folder.EXIT_POINT + "ldap_injection.xml";

		public static final String	FILE_EXIT_POINT_LOG_FORGING								= Folder.EXIT_POINT + "log_forging.xml";

		public static final String	FILE_EXIT_POINT_PATH_TRAVERSAL						= Folder.EXIT_POINT + "path_traversal.xml";
		public static final String	FILE_EXIT_POINT_REFLECTION_INJECTION			= Folder.EXIT_POINT
																																							+ "reflection_injection.xml";
		public static final String	FILE_EXIT_POINT_SECURITY_MISCONFIGURATION	= Folder.EXIT_POINT
																																							+ "security_misconfiguration.xml";

		public static final String	FILE_EXIT_POINT_SQL_INJECTION							= Folder.EXIT_POINT + "sql_injection.xml";

		public static final String	FILE_EXIT_POINT_XPATH_INJECTION						= Folder.EXIT_POINT + "xpath_injection.xml";
	}

	public abstract class Icons {
		public static final String	SECURITY_VULNERABILITY_QUICK_FIX_OPTION	= Folder.ICON + "QuickFixOption.png";
		public static final String	SECURITY_VULNERABILITY_ENTRY						= Folder.ICON + "SecVulEntry.png";
		public static final String	SECURITY_VULNERABILITY_EXIT							= Folder.ICON + "SecVulExit.png";
		public static final String	SECURITY_VULNERABILITY_MULTIPLE					= Folder.ICON + "SecVulMultiple.png";
	}

	public abstract class PrefPageSecurityVulnerability {
		public static final String	FIELD_COMMAND_INJECTION					= Activator.PLUGIN_ID + ".CommandInjection";
		public static final String	FIELD_COOKIE_POISONING					= Activator.PLUGIN_ID + ".CookiePoisoning";
		public static final String	FIELD_CROSS_SITE_SCRIPTING			= Activator.PLUGIN_ID + ".CrossSiteScripting";
		public static final String	FIELD_HTTP_RESPONSE_SPLITTING		= Activator.PLUGIN_ID + ".HttpResponseSplitting";
		public static final String	FIELD_LDAP_INJECTION						= Activator.PLUGIN_ID + ".LDAPInjection";
		public static final String	FIELD_LOG_FORGING								= Activator.PLUGIN_ID + ".LogForging";
		public static final String	FIELD_PATH_TRAVERSAL						= Activator.PLUGIN_ID + ".PathTraversal";
		public static final String	FIELD_REFLECTION_INJECTION			= Activator.PLUGIN_ID + ".ReflectionInjection";
		public static final String	FIELD_SECURITY_MISCONFIGURATION	= Activator.PLUGIN_ID + ".SecurityMisconfiguration";
		public static final String	FIELD_SQL_INJECTION							= Activator.PLUGIN_ID + ".SQLInjection";
		public static final String	FIELD_XPATH_INJECTION						= Activator.PLUGIN_ID + ".XPathInjection";

		public static final String	FIELD_MONITORED_PROJECTS				= Activator.PLUGIN_ID + ".MonitoredProjects";
	}

	public abstract class PrefPageSettings {
		public static final String	ID_PAGE											= Package.UI + ".PREFERENCE.PAGE.SETTINGS";

		public static final String	FIELD_RUN_MODE							= Activator.PLUGIN_ID + ".RunMode";

		public static final String	FIELD_OUTPUT_SECURITY_VIEW	= Activator.PLUGIN_ID + ".SecurityView";
		public static final String	FIELD_OUTPUT_TEXT_FILE			= Activator.PLUGIN_ID + ".TextFile";
		public static final String	FIELD_OUTPUT_XML_FILE				= Activator.PLUGIN_ID + ".XmlFile";
	}

	public abstract class Marker {
		public static final String	TYPE_SECURITY_VULNERABILITY	= "TCM_ESVD_TYPE_SECURITY_VULNERABILITY";
	}

	public abstract class XMLLoader {
		public static final String	TAG_SANITIZATION_POINT			= "sanitizer";
		public static final String	TAG_ENTRY_POINT							= "entrypoint";
		public static final String	TAG_EXIT_POINT							= "exitpoint";
		public static final String	TAG_QUALIFIED_NAME					= "qualifiedname";
		public static final String	TAG_METHOD_NAME							= "methodname";
		public static final String	TAG_PARAMETERS							= "parameters";
		public static final String	TAG_PARAMETERS_TYPE					= "type";
		public static final String	TAG_PARAMETERS_RULES				= "rules";

		public static final String	TAG_RESOLUTION							= "resolution";
		public static final String	TAG_RESOLUTION_TYPE					= "type";
		public static final String	TAG_RESOLUTION_LABEL				= "label";
		public static final String	TAG_RESOLUTION_DESCRIPTION	= "description";
	}

	public abstract class Vulnerability {
		// To avoid collision with the id from the Verifiers. 30 - 39
		public static final int	ENTRY_POINT																		= 30;

		// Security Misconfiguration 40 - 49
		public static final int	SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT	= 40;

		// SQL Injection 50 - 59
		public static final int	SQL_INJECTION_STRING_CONCATENATION						= 50;

		// SQL Injection 60 - 69
		public static final int	INFORMATION_LEAKAGE														= 60;
	}

}
