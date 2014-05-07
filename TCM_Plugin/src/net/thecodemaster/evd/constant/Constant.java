package net.thecodemaster.evd.constant;

import net.thecodemaster.evd.Activator;

/**
 * This class contains constants used by the application.
 * 
 * @author Luciano Sampaio
 */
public abstract class Constant {

	public static final String	JDT_NATURE														= "org.eclipse.jdt.core.javanature";
	public static final String	ID_NATURE															= Activator.ID_PLUGIN + ".TCM_EVD_NATURE";
	public static final String	ID_BUILDER														= Activator.ID_PLUGIN + ".TCM_EVD_BUILDER";
	public static final String	ID_MARKER															= Activator.ID_PLUGIN + ".TCM_EVD_MARKER";

	public static final String	SEPARATOR															= ";";
	public static final String	RESOURCE_TYPE_TO_PERFORM_DETECTION		= "java" + SEPARATOR + "jsp";
	public static final int			MAXIMUM_DEPTH													= 10;
	public static final int			ID_VERIFIER_COOKIE_POISONING					= 1;
	public static final int			ID_VERIFIER_CROSS_SITE_SCRIPTING			= 2;
	public static final int			ID_VERIFIER_SECURITY_MISCONFIGURATION	= 3;
	public static final int			ID_VERIFIER_SQL_INJECTION							= 4;

	public static final String	OBJECT																= "java.lang.Object";

	public abstract class Package {
		public static final String	UI						= "net.thecodemaster.evd.ui";
		public static final String	L10N_MESSAGES	= UI + ".l10n.message";
	}

	public abstract class Folder {
		public static final String	ICON						= "icon/";
		public static final String	KNOWLEDGE_BASE	= "knowledge_base/";
		public static final String	ENTRY_POINT			= KNOWLEDGE_BASE + "entry_point/";
		public static final String	EXIT_POINT			= KNOWLEDGE_BASE + "exit_point/";
	}

	public abstract class File {
		public static final String	FILE_ENTRY_POINT													= Folder.ENTRY_POINT + "entry_point.xml";
		public static final String	FILE_EXIT_POINT_COOKIE_POISONING					= Folder.EXIT_POINT + "cookie_poisoning.xml";
		public static final String	FILE_EXIT_POINT_CROSS_SITE_SCRIPTING			= Folder.EXIT_POINT
																																							+ "cross_site_scripting.xml";
		public static final String	FILE_EXIT_POINT_SECURITY_MISCONFIGURATION	= Folder.EXIT_POINT
																																							+ "security_misconfiguration.xml";
		public static final String	FILE_EXIT_POINT_SQL_INJECTION							= Folder.EXIT_POINT + "sql_injection.xml";
	}

	public abstract class Icons {
		public static final String	SECURITY_VULNERABILITY	= Folder.ICON + "SecurityVulnerability.png";
	}

	public abstract class PrefPageSecurityVulnerability {
		public static final String	FIELD_COOKIE_POISONING					= Activator.ID_PLUGIN + ".CookiePoisoning";
		public static final String	FIELD_CROSS_SITE_SCRIPTING			= Activator.ID_PLUGIN + ".CrossSiteScripting";
		public static final String	FIELD_SECURITY_MISCONFIGURATION	= Activator.ID_PLUGIN + ".SecurityMisconfiguration";
		public static final String	FIELD_SQL_INJECTION							= Activator.ID_PLUGIN + ".SQLInjection";
		public static final String	FIELD_UNVALIDATED_REDIRECTING		= Activator.ID_PLUGIN + ".UnvalidatedRedirecting";

		public static final String	FIELD_MONITORED_PROJECTS				= Activator.ID_PLUGIN + ".MonitoredProjects";
	}

	public abstract class PrefPageSettings {
		public static final String	ID_PAGE											= Package.UI + ".PREFERENCE.PAGE.SETTINGS";

		public static final String	FIELD_RUN_MODE							= Activator.ID_PLUGIN + ".RunMode";

		public static final String	FIELD_OUTPUT_PROBLEMS_VIEW	= Activator.ID_PLUGIN + ".ProblemsView";
		public static final String	FIELD_OUTPUT_TEXT_FILE			= Activator.ID_PLUGIN + ".TextFile";
		public static final String	FIELD_OUTPUT_XML_FILE				= Activator.ID_PLUGIN + ".XmlFile";
	}

	public abstract class Marker {
		public static final String	TYPE_SECURITY_VULNERABILITY	= "TCM_EVD_TYPE_SECURITY_VULNERABILITY";
	}

	public abstract class XMLLoader {
		public static final String	TAG_ENTRY_POINT				= "entrypoint";
		public static final String	TAG_EXIT_POINT				= "exitpoint";
		public static final String	TAG_QUALIFIED_NAME		= "qualifiedname";
		public static final String	TAG_METHOD_NAME				= "methodname";
		public static final String	TAG_PARAMETERS				= "parameters";
		public static final String	TAG_PARAMETERS_TYPE		= "type";
		public static final String	TAG_PARAMETERS_RULES	= "rules";
	}

}
