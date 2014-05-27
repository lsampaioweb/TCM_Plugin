package net.thecodemaster.evd.helper;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public abstract class HelperVerifiers {

	public static String getFilePath(int fileId) {
		switch (fileId) {
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				return Constant.File.FILE_EXIT_POINT_COMMAND_INJECTION;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				return Constant.File.FILE_EXIT_POINT_COOKIE_POISONING;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				return Constant.File.FILE_EXIT_POINT_CROSS_SITE_SCRIPTING;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				return Constant.File.FILE_EXIT_POINT_PATH_TRAVERSAL;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				return Constant.File.FILE_EXIT_POINT_SECURITY_MISCONFIGURATION;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				return Constant.File.FILE_EXIT_POINT_SQL_INJECTION;
			case Constant.VERIFIER_ID_UNVALIDATED_REDIRECTING:
				return Constant.File.FILE_EXIT_POINT_UNVALIDATED_REDIRECTING;
			default:
				String errorMessage = String.format(Message.Error.FILE_PATH_ID_NOT_FOUND, fileId);
				PluginLogger.logError(errorMessage, null);
				return null;
		}
	}

	public static String getTypeVulnerabilityName(int typeVulnerability) {
		switch (typeVulnerability) {
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				return Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				return Message.Plugin.VERIFIER_NAME_COOKIE_POISONING;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				return Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				return Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				return Message.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				return Message.Plugin.VERIFIER_NAME_SQL_INJECTION;
			case Constant.VERIFIER_ID_UNVALIDATED_REDIRECTING:
				return Message.Plugin.VERIFIER_NAME_UNVALIDATED_REDIRECTING;
			default:
				String errorMessage = String.format(Message.Error.TYPE_VULNERABILITY_NOT_FOUND, typeVulnerability);
				PluginLogger.logError(errorMessage, null);
				return null;
		}
	}
}
